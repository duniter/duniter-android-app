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
import io.ucoin.app.service.exception.HttpBadRequestException;
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

    public static final String URL_BLOCK_WITH_UD = URL_BASE + "/with/ud";

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
     * @param currencyId
     * @return
     */
    public BlockchainParameter getParameters(long currencyId) {
        // get blockchain parameter
        BlockchainParameter result = executeRequest(currencyId, URL_PARAMETERS, BlockchainParameter.class);
        return result;
    }

    /**
     * get the blockchain parameters (currency parameters)
     * @param peer the peer to use for request
     * @return
     */
    public BlockchainParameter getParameters(Peer peer) {
        // get blockchain parameter
        BlockchainParameter result = executeRequest(peer, URL_PARAMETERS, BlockchainParameter.class);
        return result;
    }

    /**
     * Retrieve a block, by id (from 0 to current)
     * @param currencyId
     * @param number
     * @return
     */
    public BlockchainBlock getBlock(long currencyId, int number) {
        // get blockchain parameter
        String path = String.format(URL_BLOCK, number);
        BlockchainBlock result = executeRequest(currencyId, path, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve a block, by id (from 0 to current)
     * @param peer the peer to use for request
     * @param number the block number
     * @return
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
     */
    public BlockchainBlock getCurrentBlock(long currencyId) {
        // get blockchain parameter
        BlockchainBlock result = executeRequest(currencyId, URL_BLOCK_CURRENT, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the current block
     * @param peer the peer to use for request
     * @return the last block
     */
    public BlockchainBlock getCurrentBlock(Peer peer) {
        // get blockchain parameter
        BlockchainBlock result = executeRequest(peer, URL_BLOCK_CURRENT, BlockchainBlock.class);
        return result;
    }

    /**
     * Retrieve the currency data, from peer
     * @param peer
     * @return
     */
    public Currency getCurrencyFromPeer(Peer peer) {
        BlockchainParameter parameter = getParameters(peer);
        BlockchainBlock firstBlock = getBlock(peer, 0);
        BlockchainBlock lastBlock = getCurrentBlock(peer);

        Currency result = new Currency();
        result.setCurrencyName(parameter.getCurrency());
        result.setFirstBlockSignature(firstBlock.getSignature());
        result.setMembersCount(lastBlock.getMembersCount());

        return result;
    }

    /**
     * Retrieve the last block with UD
     * @param currencyId id of currency
     * @return
     */
    public Integer getLastUD(long currencyId) {
        // get block number with UD
        String blocksWithUdResponse = executeRequest(currencyId, URL_BLOCK_WITH_UD, String.class);
        Integer blockNumber = getLastBlockNumberFromJson(blocksWithUdResponse);

        // If no result (this could happen when no UD has been send
        if (blockNumber == null) {
            // get the first UD from currency parameter
            BlockchainParameter parameter = getParameters(currencyId);
            return parameter.getUd0();
        }

        // Get the UD from the last block with UD
        BlockchainBlock block = getBlock(currencyId, blockNumber);
        Integer lastUD = block.getDividend();

        // Check not null (should never happend)
        if (lastUD == null) {
            throw new UCoinTechnicalException("Unable to get last UD from server");
        }
        return lastUD;
    }

     /**
     * Check is a wallet is a member, and load its attribute isMember and certTimestamp
      * @param wallet
     * @throws UidMatchAnotherPubkeyException is uid already used by another pubkey
     */
    public void loadAndCheckMembership(Peer peer, Wallet wallet) throws UidMatchAnotherPubkeyException {
        ObjectUtils.checkNotNull(wallet);

        // Load membership data
        loadMembership(null, peer, wallet.getIdentity(), true);

        // Something wrong on pubkey : uid already used by another pubkey !
        if (wallet.getIdentity().getIsMember() == null) {
            throw new UidMatchAnotherPubkeyException(wallet.getPubKeyHash());
        }
    }

    /**
     * Load identity attribute isMember and timestamp
     * @param identity
     */
    public void loadMembership(long currencyId, Identity identity, boolean checkLookupForNonMember) {
        loadMembership(currencyId, null, identity, checkLookupForNonMember);
    }


    public BlockchainMembershipResults getMembershipByUid(long currencyId, String uid) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(uid));

        BlockchainMembershipResults result = getMembershipByPubkeyOrUid(currencyId, uid);
        if (result == null || !uid.equals(result.getUid())) {
            return null;
        }
        return result;
    }

    public BlockchainMembershipResults getMembershipByPublicKey(long currencyId, String pubkey) {
        ObjectUtils.checkArgument(StringUtils.isNotBlank(pubkey));

        BlockchainMembershipResults result = getMembershipByPubkeyOrUid(currencyId, pubkey);
        if (result == null || !pubkey.equals(result.getPubkey())) {
            return null;
        }
        return result;
    }

    /**
     * Request to integrate the wot
     */
    public void requestMembership(Wallet wallet) {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(wallet.getCurrencyId());

        BlockchainBlock block = getCurrentBlock(wallet.getCurrencyId());

        // Compute membership document
        String membership = getMembership(wallet,
                block,
                true /*sideIn*/);

        Log.d(TAG, String.format(
                "Will send membership document: \n------\n%s------",
                membership));

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("membership", membership));

        HttpPost httpPost = new HttpPost(getPath(wallet.getCurrencyId(), URL_MEMBERSHIP));
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

    protected void loadMembership(Long currencyId, Peer peer, Identity identity, boolean checkLookupForNonMember) {
        ObjectUtils.checkNotNull(identity);
        ObjectUtils.checkArgument(StringUtils.isNotBlank(identity.getUid()));
        ObjectUtils.checkArgument(StringUtils.isNotBlank(identity.getPubkey()));
        ObjectUtils.checkArgument(peer != null || currencyId != null);

        // Read membership data from the UID
        BlockchainMembershipResults result = peer != null
                ? getMembershipByPubkeyOrUid(peer, identity.getUid())
                : getMembershipByPubkeyOrUid(currencyId, identity.getUid());

        // uid not used = not a member
        if (result == null) {
            identity.setMember(false);

            if (checkLookupForNonMember) {
                WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
                Identity lookupIdentity = wotService.getIdentity(currencyId, identity.getUid(), identity.getPubkey());

                // Self certification exists, update the cert timestamp
                if (lookupIdentity != null) {
                    identity.setTimestamp(lookupIdentity.getTimestamp());
                }

                // Self certitification not exists: make sure the cert time is cleaning
                else {
                    identity.setTimestamp(-1);
                }
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

    public BlockchainMembershipResults getMembershipByPubkeyOrUid(long currencyId, String uidOrPubkey) {
        String path = String.format(URL_MEMBERSHIP_SEARCH, uidOrPubkey);

        // search blockchain membership
        try {
            BlockchainMembershipResults result = executeRequest(currencyId, path, BlockchainMembershipResults.class);
            return result;
        }
        catch(HttpBadRequestException e) {
            Log.d(TAG, "No member matching this pubkey or uid: " + uidOrPubkey);
            return null;
        }
    }

    public BlockchainMembershipResults getMembershipByPubkeyOrUid(Peer peer, String uidOrPubkey) {
        String path = String.format(URL_MEMBERSHIP_SEARCH, uidOrPubkey);

        // search blockchain membership
        try {
            BlockchainMembershipResults result = executeRequest(peer, path, BlockchainMembershipResults.class);
            return result;
        }
        catch(HttpBadRequestException e) {
            Log.d(TAG, "No member matching this pubkey or uid: " + uidOrPubkey);
            return null;
        }
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

    private Integer getLastBlockNumberFromJson(final String json) {

        int startIndex = json.lastIndexOf(',');
        int endIndex = json.lastIndexOf(']');
        if (startIndex == -1 || endIndex == -1) {
            return null;
        }

        String blockNumberStr = json.substring(startIndex+1,endIndex).trim();
        try {
            return Integer.parseInt(blockNumberStr);
        } catch(NumberFormatException e) {
            Log.e(TAG, "Could not parse JSON (block numbers)");
            throw new UCoinTechnicalException("Could not parse server response");
        }
    }
}
