package io.ucoin.app.service.remote;

import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Peer;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.remote.BlockchainMembershipResults;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;

public class BlockchainRemoteService extends BaseRemoteService {

    private static final String TAG = "BlockchainRemoteService";


    public static final String URL_BASE = "/blockchain";

    public static final String URL_PARAMETERS = URL_BASE + "/parameters";

    public static final String URL_BLOCK = URL_BASE + "/block/%s";

    public static final String URL_BLOCK_CURRENT = URL_BASE + "/current";

    public static final String URL_MEMBERSHIP = URL_BASE + "/membership";

    public static final String URL_MEMBERSHIP_SEARCH = URL_BASE + "/memberships/%s";

    private NetworkRemoteService networkRemoteService;

    public BlockchainRemoteService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        networkRemoteService = ServiceLocator.instance().getNetworkRemoteService();
    }

    /**
     * get the blockchain parameters (currency parameters)
     * @return
     * @throws Exception
     */
    public BlockchainParameter getParameters() {
        // get blockchain parameter
        BlockchainParameter result = executeRequest(URL_PARAMETERS, BlockchainParameter.class);
        return result;
    }

    /**
     * get the blockchain parameters (currency parameters)
     * @param peer the peer to use for request
     * @return
     * @throws Exception
     */
    public BlockchainParameter getParameters(Peer peer) {
        // get blockchain parameter
        BlockchainParameter result = executeRequest(peer, URL_PARAMETERS, BlockchainParameter.class);
        return result;
    }

    /**
     * Retrieve a block, by id (from 0 to current)
     * @param number
     * @return
     * @throws Exception
     */
    public BlockchainBlock getBlock(int number) {
        // get blockchain parameter
        String path = String.format(URL_BLOCK, number);
        BlockchainBlock result = executeRequest(path, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve a block, by id (from 0 to current)
     * @param peer the peer to use for request
     * @param number the block number
     * @return
     * @throws Exception
     */
    public BlockchainBlock getBlock(Peer peer, int number) {
        // get blockchain parameter
        String path = String.format(URL_BLOCK, number);
        BlockchainBlock result = executeRequest(peer, path, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the current block
     * @return
     * @throws Exception
     */
    public BlockchainBlock getCurrentBlock() {
        // get blockchain parameter
        BlockchainBlock result = executeRequest(URL_BLOCK_CURRENT, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the current block
     * @param peer the peer to use for request
     * @return the last block
     * @throws Exception
     */
    public BlockchainBlock getCurrentBlock(Peer peer) {
        // get blockchain parameter
        BlockchainBlock result = executeRequest(peer, URL_BLOCK_CURRENT, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the currency data, from peer
     * @return
     * @throws Exception
     */
    public Currency getCurrencyFromPeer(Peer peer) {
        BlockchainParameter parameter = getParameters(peer);
        BlockchainBlock firstBlock = getBlock(peer, 0);
        BlockchainBlock lastBlock = getCurrentBlock(peer);

        Currency result = new Currency();
        result.setCurrencyName(parameter.getCurrency());
        result.setFirstBlockSignature(firstBlock.getSignature());
        result.setMembersCount(lastBlock.getMembersCount());

        // TODO make getPeers works
        //networkRemoteService.getPeers(peer);

        return result;
    }


     /**
     * Check is a wallet is a member, and load its attribute isMember and certTimestamp
      * @param wallet
     * @throws UidMatchAnotherPubkeyException
     */
    public void loadAndCheckMembership(Wallet wallet) throws UidMatchAnotherPubkeyException {
        ObjectUtils.checkNotNull(wallet);

        // Load membership data
        loadMembership(wallet.getIdentity());

        // Something wrong on pubkey : uid already used by another pubkey !
        if (wallet.getIdentity().getIsMember() == null) {
            throw new UidMatchAnotherPubkeyException(wallet.getPubKeyHash());
        }
    }

    /**
     * Load identity attribute isMember and timestamp
     * @param identity
     * @throws UidMatchAnotherPubkeyException
     */
    public void loadMembership(Identity identity) {
        ObjectUtils.checkNotNull(identity);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(identity.getUid()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(identity.getPubkey()));

        // Read membership data from the UID
        BlockchainMembershipResults result = getMembershipByPubkeyOrUid(identity.getUid());

        // uid not used = not a member
        if (result == null) {
            identity.setMember(false);

            // Try to find a self certification
            WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
            Identity lookupIdentity = wotService.getIdentity(identity.getUid(), identity.getPubkey());

            // Self certification exists, update the cert timestamp
            if (lookupIdentity != null) {
                identity.setTimestamp(lookupIdentity.getTimestamp());
            }

            // Self certitification not exists: make sure the cert time is reseted
            else {
                identity.setTimestamp(-1);
            }
        }

        // UID and pubkey is a member: fine
        else if (identity.getPubkey().equals(result.getPubkey())) {
            identity.setMember(true);
            identity.setTimestamp(result.getSigDate());
        }

        // Something wrong on pubkey : uid already used by anither pubkey !
        else {
            identity.setMember(null);
        }

    }


    public BlockchainMembershipResults getMembershipByUid(Peer peer, String uid) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(uid));

        BlockchainMembershipResults result = getMembershipByPubkeyOrUid(uid);
        if (result == null || !uid.equals(result.getUid())) {
            return null;
        }
        return result;
    }

    public BlockchainMembershipResults getMembershipByPublicKey(String pubkey) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(pubkey));

        BlockchainMembershipResults result = getMembershipByPubkeyOrUid(pubkey);
        if (result == null || !pubkey.equals(result.getPubkey())) {
            return null;
        }
        return result;
    }

    /**
     * Request to integrate the wot
     * @throws Exception
     */
    public void requestMembership(Wallet wallet) {

        BlockchainBlock block = getCurrentBlock();

        // Compute memebership document
        String membership = getMembership(wallet,
                block,
                true /*sideIn*/);

        Log.d(TAG, String.format(
                "Will send membership document: \n------\n%s------",
                membership));

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("membership", membership));

        HttpPost httpPost = new HttpPost(getPath(URL_MEMBERSHIP));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            throw new UCoinTechnicalException(e);
        }

        String membershipResult = executeRequest(httpPost, String.class);
        Log.d(TAG, "received from /tx/process: " + membershipResult);


        executeRequest(httpPost, null);
    }


    /* -- Internal methods -- */

    public BlockchainMembershipResults getMembershipByPubkeyOrUid(String uidOrPubkey) {
        String path = String.format(URL_MEMBERSHIP_SEARCH, uidOrPubkey);

        // search blockchain membership
        BlockchainMembershipResults result = executeRequest(path, BlockchainMembershipResults.class);
        return result;
    }

    public String getMembership(Wallet wallet,
                                BlockchainBlock block,
                                boolean sideIn
                                ) {

        // Create the member ship document
        String membership = getMembership(wallet.getUid(),
                wallet.getPubKeyHash(),
                wallet.getCurrency(),
                block.getNumber(),
                block.getHash(),
                sideIn,
                wallet.getCertTimestamp()
        );

        // Add signature
        CryptoService cryptoService = ServiceLocator.instance().getCryptoService();
        String signature = cryptoService.sign(membership, wallet.getSecKey());

        return new StringBuilder().append(membership).append(signature)
                .append('\n').toString();
    }

    private String getMembership(String uid,
                                 String publicKey,
                                 String currency,
                                 long blockNumber,
                                 String blockHash,
                                 boolean sideIn,
                                 long certificationTime
    ) {
        StringBuilder result = new StringBuilder()
                .append("Version: 1\n")
                .append("Type: Membership\n")
                .append("Currency: ").append(currency).append('\n')
                .append("Issuer: ").append(publicKey).append('\n')
                .append("Block: ").append(blockNumber).append('-').append(blockHash).append('\n')
                .append("Membership: ").append(sideIn ? "IN" : "OUT").append('\n')
                .append("UserID: ").append(uid).append('\n')
                .append("CertTS: ").append(certificationTime).append('\n');

        return result.toString();
    }
}
