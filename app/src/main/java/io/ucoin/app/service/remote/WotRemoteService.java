package io.ucoin.app.service.remote;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.WotIdentityCertifications;
import io.ucoin.app.model.WotLookupResult;
import io.ucoin.app.model.WotLookupResults;
import io.ucoin.app.model.WotLookupUId;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;

public class WotRemoteService extends BaseRemoteService {

    private static final String TAG = "WotService";

    public static final String URL_BASE = "/wot";

    public static final String URL_ADD = URL_BASE + "/add";

    public static final String URL_LOOKUP = URL_BASE + "/lookup/%s";

    public static final String URL_CERTIFIED_BY = URL_BASE + "/certified-by/%s";

    public static final String URL_CERTIFIERS_OF = URL_BASE + "/certifiers-of/%s";

    /**
     * See https://github.com/ucoin-io/ucoin-cli/blob/master/bin/ucoin
     * > var hash = res.current ? res.current.hash : 'DA39A3EE5E6B4B0D3255BFEF95601890AFD80709';
     */
    public static final String BLOCK_ZERO_HASH = "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709";

    public CryptoService cryptoService;

    public WotRemoteService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        cryptoService = ServiceLocator.instance().getCryptoService();
    }

    public WotLookupResults find(String uidPattern) {
        Log.d(TAG, String.format("Try to find user info by uid: %s", uidPattern));

        // get parameter
        String path = String.format(URL_LOOKUP, uidPattern);
        WotLookupResults lookupResult = executeRequest(path, WotLookupResults.class);

        return lookupResult;

    }

    public WotLookupUId findByUid(String uid) {
        Log.d(TAG, String.format("Try to find user info by uid: %s", uid));

        // call lookup
        String path = String.format(URL_LOOKUP, uid);
        WotLookupResults lookupResults = executeRequest(path, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId uniqueResult = getUid(lookupResults, uid);
        if (uniqueResult == null) {
            return null;
        }
        
        return uniqueResult;
    }

    public WotLookupUId findByUidAndPublicKey(String uid, String pubKey) {
        Log.d(TAG, String.format("Try to find user info by uid [%s] and pubKey [%s]", uid, pubKey));

        // call lookup
        String path = String.format(URL_LOOKUP, uid);
        WotLookupResults lookupResults = executeRequest(path, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId uniqueResult = getUidByUidAndPublicKey(lookupResults, uid, pubKey);
        if (uniqueResult == null) {
            return null;
        }

        return uniqueResult;
    }

    public Identity getIdentity(String uid, String pubKey) {
        Log.d(TAG, String.format("Get identity by uid [%s] and pubKey [%s]", uid, pubKey));

        WotLookupUId lookupUid = findByUidAndPublicKey(uid, pubKey);
        if (lookupUid == null) {
            return null;
        }
        return toIdentity(lookupUid);
    }

    public WotIdentityCertifications getCertifiedBy(String uid) {
        Log.d(TAG, String.format("Try to get certifications done by uid: %s", uid));

        // call certified-by
        String path = String.format(URL_CERTIFIED_BY, uid);
        WotIdentityCertifications result = executeRequest(path, WotIdentityCertifications.class);
        
        return result;

    }
    
    public WotIdentityCertifications getCertifiersOf(String uid) {
        Log.d(TAG, String.format("Try to get certifications done to uid: %s", uid));

        // call certifiers-of
        String path = String.format(URL_CERTIFIERS_OF, uid);
        WotIdentityCertifications result = executeRequest(path, WotIdentityCertifications.class);
        
        return result;
    }

    public String sendSelf(byte[] pubKey, byte[] secKey, String uid) {
        return sendSelf(
                    pubKey,
                    secKey,
                    uid,
                    DateUtils.getCurrentTimestamp());
    }

	public String sendSelf(byte[] pubKey, byte[] secKey, String uid, long timestamp) {
		// http post /wot/add
        HttpPost httpPost = new HttpPost(getPath(URL_ADD));

        // Compute the pub key hash
        String pubKeyHash = CryptoUtils.encodeBase58(pubKey);

        // compute the self-certification
		String selfCertification = getSelfCertification(secKey, uid, timestamp);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("pubkey", pubKeyHash));
		urlParameters.add(new BasicNameValuePair("self", selfCertification));
		urlParameters.add(new BasicNameValuePair("other", ""));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        }
        catch(UnsupportedEncodingException e) {
            throw new UCoinTechnicalException(e);
        }
        String selfResult = executeRequest(httpPost, String.class);
        Log.d(TAG, "received from /add: " + selfResult);

        return selfResult;
	}

    public String sendCertification(Wallet wallet,
                                    Identity identity) {
        return sendCertification(
                    wallet.getPubKey(),
                    wallet.getSecKey(),
                    wallet.getIdentity().getUid(),
                    wallet.getIdentity().getTimestamp(),
                    identity.getUid(),
                    identity.getPubkey(),
                    identity.getTimestamp(),
                    identity.getSignature());
    }

    public String sendCertification(byte[] pubKey, byte[] secKey,
                                  String uid, long timestamp,
                                  String userUid, String userPubKeyHash,
                                  long userTimestamp, String userSignature) {
        // http post /wot/add
        HttpPost httpPost = new HttpPost(getPath(URL_ADD));

        // Read the current block (number and hash)
        BlockchainRemoteService blockchainService = ServiceLocator.instance().getBlockchainRemoteService();
        BlockchainBlock currentBlock = blockchainService.getCurrentBlock();
        int blockNumber = currentBlock.getNumber();
        String blockHash = (blockNumber != 0)
                ? currentBlock.getHash()
                : BLOCK_ZERO_HASH;

        // Compute the pub key hash
        String pubKeyHash = CryptoUtils.encodeBase58(pubKey);

        // compute the self-certification
        String selfCertification = getSelfCertification(userUid, userTimestamp, userSignature);

        // Compute the certification
        String certification = getCertification(pubKey, secKey,
                userUid, userTimestamp, userSignature,
                blockNumber, blockHash);
        String inlineCertification = toInlineCertification(pubKeyHash, userPubKeyHash, certification);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("pubkey", userPubKeyHash));
        urlParameters.add(new BasicNameValuePair("self", selfCertification));
        urlParameters.add(new BasicNameValuePair("other", inlineCertification));

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));
        }
        catch(UnsupportedEncodingException e) {
            throw new UCoinTechnicalException(e);
        }
        String selfResult = executeRequest(httpPost, String.class);
        Log.d(TAG, "received from /add: " + selfResult);

        return selfResult;
    }

    public List<Identity> toIdentities(WotLookupResults lookupResults) {
        List<Identity> result = new ArrayList<>();

        for (WotLookupResult lookupResult: lookupResults.getResults()) {
            String pubKey = lookupResult.getPubkey();
            for (WotLookupUId source: lookupResult.getUids()) {
                // Create and fill an identity, from a result row
                Identity target = new Identity();
                toIdentity(source, target);

                // fill the pub key
                target.setPubkey(pubKey);

                result.add(target);
            }
        }
        return result;
    }

    public Identity toIdentity(WotLookupUId source) {
        Identity target = new Identity();
        toIdentity(source, target);
        return target;
    }

    public void toIdentity(WotLookupUId source, Identity target) {

        target.setUid(source.getUid());
        target.setSelf(source.getSelf());
        String timestampStr = source.getMeta().get("timestamp");
        if (!TextUtils.isEmpty(timestampStr)) {
            target.setTimestamp(Long.parseLong(timestampStr));
        }
    }

    /* -- Internal methods -- */

    protected String getSelfCertification(byte[] secKey, String uid, long timestamp) {
        // Create the self part to sign
        StringBuilder buffer = new StringBuilder()
                .append("UID:")
                .append(uid)
                .append("\nMETA:TS:")
                .append(timestamp)
                .append('\n');

        // Compute the signature
        String signature = cryptoService.sign(buffer.toString(), secKey);

        // Append the signature
        return buffer.append(signature)
                .append('\n')
                .toString();
    }

    protected String toInlineCertification(String pubKeyHash,
                                           String userPubKeyHash,
                                           String certification) {
        // Read the signature
        String[] parts = certification.split("\n");
        if (parts.length != 5) {
            throw new UCoinTechnicalException("Bad certification document: " + certification);
        }
        String signature = parts[parts.length-1];

        // Read the block number
        parts = parts[parts.length-2].split(":");
        if (parts.length != 3) {
            throw new UCoinTechnicalException("Bad certification document: " + certification);
        }
        parts = parts[2].split("-");
        if (parts.length != 2) {
            throw new UCoinTechnicalException("Bad certification document: " + certification);
        }
        String blockNumber = parts[0];

        return new StringBuilder()
                .append(pubKeyHash)
                .append(':')
                .append(userPubKeyHash)
                .append(':')
                .append(blockNumber)
                .append(':')
                .append(signature)
                .append('\n')
                .toString();
    }

    protected String getCertification(byte[] pubKey, byte[] secKey, String userUid,
                                   long userTimestamp,
                                   String userSignature,
                                   int blockNumber,
                                   String blockHash) {
        // Create the self part to sign
        String unsignedCertification = getCertificationUnsigned(
                userUid, userTimestamp, userSignature, blockNumber, blockHash);

        // Compute the signature
        String signature = cryptoService.sign(unsignedCertification, secKey);

        // Append the signature
        return new StringBuilder()
                .append(unsignedCertification)
                .append(signature)
                .append('\n')
                .toString();
    }

    protected String getCertificationUnsigned(String userUid,
                                      long userTimestamp,
                                      String userSignature,
                                      int blockNumber,
                                      String blockHash) {
        // Create the self part to sign
        return new StringBuilder()
                .append("UID:")
                .append(userUid)
                .append("\nMETA:TS:")
                .append(userTimestamp)
                .append('\n')
                .append(userSignature)
                .append("\nMETA:TS:")
                .append(blockNumber)
                .append('-')
                .append(blockHash)
                .append('\n').toString();
    }

    protected String getSelfCertification(String uid,
                                              long timestamp,
                                              String signature) {
        // Create the self part to sign
        return new StringBuilder()
                .append("UID:")
                .append(uid)
                .append("\nMETA:TS:")
                .append(timestamp)
                .append('\n')
                .append(signature)
                // FIXME : in ucoin, no '\n' here - is it a bug ?
                //.append('\n')
                .toString();
    }

    protected WotLookupUId getUid(WotLookupResults lookupResults, String filterUid) {
        if (lookupResults.getResults() == null || lookupResults.getResults().size() == 0) {
            return null;
        }

        for (WotLookupResult result : lookupResults.getResults()) {
            if (result.getUids() != null && result.getUids().size() > 0) {
                for (WotLookupUId uid : result.getUids()) {
                    if (filterUid.equals(uid.getUid())) {
                        return uid;
                    }
                }
            }
        }
        
        return null;
    }

    protected WotLookupUId getUidByUidAndPublicKey(WotLookupResults lookupResults,
                                                   String filterUid,
                                                   String filterPublicKey) {
        if (lookupResults.getResults() == null || lookupResults.getResults().size() == 0) {
            return null;
        }

        for (WotLookupResult result : lookupResults.getResults()) {
            if (filterPublicKey.equals(result.getPubkey())) {
                if (result.getUids() != null && result.getUids().size() > 0) {
                    for (WotLookupUId uid : result.getUids()) {
                        if (filterUid.equals(uid.getUid())) {
                            return uid;
                        }
                    }
                }
                break;
            }
        }

        return null;
    }

}
