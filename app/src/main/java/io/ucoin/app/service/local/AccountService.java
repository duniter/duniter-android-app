package io.ucoin.app.service.local;

import android.accounts.AccountManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.content.Provider;
import io.ucoin.app.model.local.Account;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.BaseService;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.HttpService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.service.exception.PubkeyAlreadyUsedException;
import io.ucoin.app.service.exception.UidAlreadyUsedException;
import io.ucoin.app.service.exception.UidAndPubkeyNotFoundException;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.Base58;
import io.ucoin.app.technical.crypto.KeyPair;
import io.ucoin.app.technical.task.NullProgressModel;
import io.ucoin.app.technical.task.ProgressModel;

/**
 * Created by eis on 07/02/15.
 */
public class AccountService extends BaseService {

    /** Logger. */
    private static final String TAG = "AccountService";

    public AccountService() {
        super();
    }

    public Account create(
            Context context,
            String uid,
            String salt,
            String password,
            boolean isNewRegistration,
            Peer peer) throws DuplicatePubkeyException, UidMatchAnotherPubkeyException,
            PeerConnectionException,
            UidAlreadyUsedException,
            PubkeyAlreadyUsedException,
            UidAndPubkeyNotFoundException {
        return create(
                context,
                uid,
                salt,
                password,
                isNewRegistration,
                peer,
                new NullProgressModel());
    }

    public Account create(
            Context context,
            String uid,
            String salt,
            String password,
            boolean isNewRegistration,
            Peer peer,
            ProgressModel progressModel) throws DuplicatePubkeyException,
            UidMatchAnotherPubkeyException, PeerConnectionException,
            UidAlreadyUsedException,
            PubkeyAlreadyUsedException,
            UidAndPubkeyNotFoundException{

        progressModel.setProgress(0);
        progressModel.setMax(10);

        // Generate keys
        progressModel.setMessage(context.getString(R.string.computing_keys));
        CryptoService service = ServiceLocator.instance().getCryptoService();
        KeyPair keys = service.getKeyPair(salt, password);
        String pubKeyHash = Base58.encode(keys.getPubKey());

        // Connecting to peer
        progressModel.increment(context.getString(R.string.connecting_peer, peer.getUrl()));
        HttpService httpService = ServiceLocator.instance().getHttpService();
        httpService.connect(peer);

        // Get the currency from peer
        progressModel.increment(context.getString(R.string.loading_currency, peer.getUrl()));
        BlockchainRemoteService blockchainService = ServiceLocator.instance().getBlockchainRemoteService();
        Currency currency = blockchainService.getCurrencyFromPeer(peer);

        // Get if identity is a member, and get the self cert timestamp
        progressModel.increment(context.getString(R.string.loading_membership));
        Wallet wallet = new Wallet(currency.getCurrencyName(),
                uid,
                keys.getPubKey(),
                null /*do no save the secret key of the main account*/
        );
        if (progressModel.isCancelled()) {
            return null;
        }

        // If new registration: check that UID and pubkey are not already used
        if (isNewRegistration) {
            blockchainService.checkNotMemberIdentity(peer, wallet.getIdentity());
            wallet.getIdentity().setMember(false);
        }
        // If login, check UID and pubkey exists, and load membership state
        else {
            blockchainService.loadAndCheckMembership(peer, wallet);

            if (wallet.getIdentity().getTimestamp() == -1) {
                throw new UidAndPubkeyNotFoundException(wallet.getPubKeyHash());
            }
        }

        if (progressModel.isCancelled()) {
            return null;
        }

        // Get credit
        Long credit = null;
        if (!isNewRegistration) {
            progressModel.increment(context.getString(R.string.loading_wallet_credit));
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();
            credit = txService.getCredit(peer, pubKeyHash);
        }
        else {
            progressModel.increment();
        }

        if (progressModel.isCancelled()) {
            return null;
        }

        // Create account in DB
        Account account;
        {
            progressModel.increment(context.getString(R.string.saving_account));
            AccountService accountService = ServiceLocator.instance().getAccountService();
            account = new Account();
            account.setUid(uid);
            account.setPubkey(pubKeyHash);
            account.setSalt(salt);
            account = save(context, account);
        }

        // Create the currency in DB
        {
            progressModel.increment(context.getString(R.string.saving_currency, peer.getUrl()));
            CurrencyService currencyService = ServiceLocator.instance().getCurrencyService();
            currency.setAccountId(account.getId());
            currency = currencyService.save(context, currency);
        }

        // Create the peer in DB
        {
            progressModel.increment(context.getString(R.string.saving_peer));
            PeerService peerService = ServiceLocator.instance().getPeerService();
            peer.setCurrencyId(currency.getId());
            peerService.save(context, peer);
            // Load caches - need for calling walletService.sendSelfAndSave()
            peerService.loadCache(context, account.getId());
        }


        // Create the main wallet in DB
        {
            progressModel.increment(context.getString(R.string.saving_wallet));
            wallet.setUid(account.getUid());
            wallet.setSalt(salt);
            wallet.setName(account.getUid());
            wallet.setCurrencyId(currency.getId());
            wallet.setAccountId(account.getId());
            wallet.setCredit(credit == null ? 0 : credit);
            WalletService walletService = ServiceLocator.instance().getWalletService();
            wallet = walletService.save(context, wallet);
        }


        if (progressModel.isCancelled()) {
            return null;
        }

        // Send registration
        if (isNewRegistration) {
            progressModel.increment(context.getString(R.string.sending_certification));
            WalletService walletService = ServiceLocator.instance().getWalletService();

            // Set the secret key into the wallet (will be available for this session only)
            // (must be done AFTER the walletService.save(), to avoid to write in DB)
            wallet.setSecKey(keys.getSecKey());

            walletService.sendSelfAndSave(context, wallet);

            // Reset the secret key
            wallet.setSecKey(null);
        }
        else {
            progressModel.increment();
        }

        return account;
    }

    public Account save(final Context context, final Account account) {
        ObjectUtils.checkNotNull(account);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(account.getUid()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(account.getPubkey()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(account.getSalt()));

        // Create
        if (account.getId() == null) {
            return insert(context, account);
        }

        // TODO : update
        return null;
    }

    /* -- internal methods-- */


    public Account insert(final Context context, final Account account) {

        //Create account in database
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Account.UID, account.getUid());
        values.put(SQLiteTable.Account.PUBLIC_KEY, account.getPubkey());
        values.put(SQLiteTable.Account.SALT, account.getSalt());
        values.put(SQLiteTable.Account.CRYPT_PIN, account.getCryptPin());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        uri = context.getContentResolver().insert(uri, values);
        Long accountId = ContentUris.parseId(uri);
        if (accountId < 0) {
            throw new UCoinTechnicalException("Error while inserting account");
        }

        //create account in android framework
        Bundle data = new Bundle();
        data.putString(SQLiteTable.Account._ID, accountId.toString());
        data.putString(SQLiteTable.Account.PUBLIC_KEY, account.getPubkey());
        android.accounts.Account androidAccount = new android.accounts.Account(account.getUid(), context.getString(R.string.ACCOUNT_TYPE));
        AccountManager.get(context).addAccountExplicitly(androidAccount, null, data);

        //keep a reference to the last account used
        SharedPreferences.Editor editor =
                context.getSharedPreferences("account", Context.MODE_PRIVATE).edit();
        editor.putString("_id", accountId.toString());
        editor.apply();

        // Refresh the inserted account
        account.setId(accountId);

        return account;
    }

}
