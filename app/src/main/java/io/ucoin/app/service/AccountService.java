package io.ucoin.app.service;

import android.accounts.AccountManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Account;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Peer;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.DummyProgressModel;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.ProgressModel;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.Base58;
import io.ucoin.app.technical.crypto.KeyPair;

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
            Peer peer) throws DuplicatePubkeyException, UidMatchAnotherPubkeyException, PeerConnectionException {
        return create(
                context,
                uid,
                salt,
                password,
                peer,
                new DummyProgressModel());
    }

    public Account create(
            Context context,
            String uid,
            String salt,
            String password,
            Peer peer,
            ProgressModel progressModel) throws DuplicatePubkeyException, UidMatchAnotherPubkeyException, PeerConnectionException {

        progressModel.setProgress(0);
        progressModel.setMax(9);


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
        blockchainService.loadAndCheckMembership(wallet);

        // Get credit
        progressModel.increment(context.getString(R.string.loading_wallet_credit));
        TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();
        Long credit = txService.getCredit(peer, pubKeyHash);

        // Create account in DB
        io.ucoin.app.model.Account account;
        {
            progressModel.increment(context.getString(R.string.saving_account));
            AccountService accountService = ServiceLocator.instance().getAccountService();
            account = new io.ucoin.app.model.Account();
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

        // Create the main wallet in DB
        {
            progressModel.increment(context.getString(R.string.saving_wallet));
            wallet.setUid(account.getUid());
            wallet.setSalt(salt);
            wallet.setName(account.getUid() + "@" + currency.getCurrencyName());
            wallet.setCurrencyId(currency.getId());
            wallet.setAccountId(account.getId());
            wallet.setCredit(credit == null ? 0 : credit.intValue());
            WalletService walletService = ServiceLocator.instance().getWalletService();
            wallet = walletService.save(context, wallet);
        }

        // Create the peer in DB
        {
            progressModel.increment(context.getString(R.string.saving_peer));
            peer.setCurrencyId(currency.getId());
            ServiceLocator.instance().getPeerService().save(context, peer);
        }

        progressModel.increment(context.getString(R.string.starting_home));

        // Set the secret key into the wallet (will be available for this session only)
        // (must be done AFTER the walletService.save(), to avoid to write in DB)
        wallet.setSecKey(keys.getSecKey());

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
        values.put(Contract.Account.UID, account.getUid());
        values.put(Contract.Account.PUBLIC_KEY, account.getPubkey());
        values.put(Contract.Account.SALT, account.getSalt());
        values.put(Contract.Account.CRYPT_PIN, account.getCryptPin());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        uri = context.getContentResolver().insert(uri, values);
        Long accountId = ContentUris.parseId(uri);
        if (accountId < 0) {
            throw new UCoinTechnicalException("Error while inserting account");
        }

        //create account in android framework
        Bundle data = new Bundle();
        data.putString(Contract.Account._ID, accountId.toString());
        data.putString(Contract.Account.PUBLIC_KEY, account.getPubkey());
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
