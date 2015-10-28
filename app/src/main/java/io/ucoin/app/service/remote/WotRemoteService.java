package io.ucoin.app.service.remote;

import android.text.TextUtils;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.BlockchainBlock;
import io.ucoin.app.model.remote.BlockchainParameters;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.model.remote.WotIdentityCertifications;
import io.ucoin.app.model.remote.WotLookupResult;
import io.ucoin.app.model.remote.WotLookupResults;
import io.ucoin.app.model.remote.WotLookupSignature;
import io.ucoin.app.model.remote.WotLookupUId;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.ModelUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;

public class WotRemoteService extends BaseRemoteService {

    private static final String TAG = "WotRemoteService";

    public static final String URL_BASE = "/wot";

    public static final String URL_ADD = URL_BASE + "/add";

    public static final String URL_LOOKUP = URL_BASE + "/lookup/%s";

    public static final String URL_REQUIREMENT = URL_BASE+"/requirements/%s";

    public static final String URL_CERTIFIED_BY = URL_BASE + "/certified-by/%s";

    public static final String URL_CERTIFIERS_OF = URL_BASE + "/certifiers-of/%s";

    /**
     * See https://github.com/ucoin-io/ucoin-cli/blob/master/bin/ucoin
     * > var hash = res.current ? res.current.hash : 'DA39A3EE5E6B4B0D3255BFEF95601890AFD80709';
     */
    public static final String BLOCK_ZERO_HASH = "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709";

    private CryptoService cryptoService;
    private BlockchainRemoteService bcService;

    public WotRemoteService() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();
        cryptoService = ServiceLocator.instance().getCryptoService();
        bcService = ServiceLocator.instance().getBlockchainRemoteService();
    }

    public List<Identity> findIdentities(Set<Long> currenciesIds, String uidOrPubKey) {
        List<Identity> result = new ArrayList<Identity>();

        String path = String.format(URL_LOOKUP, uidOrPubKey);

        for (Long currencyId: currenciesIds) {

            WotLookupResults lookupResult = executeRequest(currencyId, path, WotLookupResults.class);

            addAllIdentities(result, lookupResult, currencyId);
        }

        return result;
    }

    public WotLookupUId find(long currencyId, String uidOrPubKey) {
        Log.d(TAG, String.format("Try to find user by looking up on [%s]", uidOrPubKey));
        // get parameter
        String path = String.format(URL_LOOKUP, uidOrPubKey);
        String pathrequirement = String.format(URL_REQUIREMENT, uidOrPubKey);
        WotLookupResults lookupResults = executeRequest(currencyId, path, WotLookupResults.class);

        for (WotLookupResult result : lookupResults.getResults()) {
            if (result.getUids() != null && result.getUids().size() > 0) {
                for (WotLookupUId uid : result.getUids()) {
                    return uid;
                }
            }
        }
        return null;

    }

    public WotLookupUId findByUid(long currencyId, String uid) {
        Log.d(TAG, String.format("Try to find user info by uid: %s", uid));

        // call lookup
        String path = String.format(URL_LOOKUP, uid);
        WotLookupResults lookupResults = executeRequest(currencyId, path, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId uniqueResult = getUid(lookupResults, uid);
        if (uniqueResult == null) {
            return null;
        }
        
        return uniqueResult;
    }

    public WotLookupUId findByUidAndPublicKey(long currencyId, String uid, String pubKey) {
        Log.d(TAG, String.format("Try to find user info by uid [%s] and pubKey [%s]", uid, pubKey));

        // call lookup
        String path = String.format(URL_LOOKUP, uid);
        WotLookupResults lookupResults = executeRequest(currencyId, path, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId uniqueResult = getUidByUidAndPublicKey(lookupResults, uid, pubKey);
        if (uniqueResult == null) {
            return null;
        }

        return uniqueResult;
    }

    public WotLookupUId findByUidAndPublicKey(Peer peer, String uid, String pubKey) {
        Log.d(TAG, String.format("Try to find user info by uid [%s] and pubKey [%s]", uid, pubKey));

        // call lookup
        String path = String.format(URL_LOOKUP, uid);
        WotLookupResults lookupResults = executeRequest(peer, path, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId uniqueResult = getUidByUidAndPublicKey(lookupResults, uid, pubKey);
        if (uniqueResult == null) {
            return null;
        }

        return uniqueResult;
    }

    public Identity getIdentity(long currencyId, String uid, String pubKey) {
        Log.d(TAG, String.format("Get identity by uid [%s] and pubKey [%s]", uid, pubKey));

        WotLookupUId lookupUid = findByUidAndPublicKey(currencyId, uid, pubKey);
        if (lookupUid == null) {
            return null;
        }
        return toIdentity(lookupUid);
    }

    public Identity getIdentity(long currencyId, String pubKey) {
//        Log.d(TAG, String.format("Get identity by uid [%s] and pubKey [%s]", uid, pubKey));

        WotLookupUId lookupUid = find(currencyId, pubKey);
        if (lookupUid == null) {
            return null;
        }
        Identity result = toIdentity(lookupUid);
        result.setPubkey(pubKey);
        result.setCurrencyId(currencyId);
        return result;
    }

    public Identity getIdentity(Peer peer, String uid, String pubKey) {
        Log.d(TAG, String.format("Get identity by uid [%s] and pubKey [%s]", uid, pubKey));

        WotLookupUId lookupUid = findByUidAndPublicKey(peer, uid, pubKey);
        if (lookupUid == null) {
            return null;
        }
        return toIdentity(lookupUid);
    }

    public Collection<WotCertification> getCertifications(long currencyId, String uid, String pubkey, boolean isMember) {
        ObjectUtils.checkNotNull(uid);
        ObjectUtils.checkNotNull(pubkey);

        if (isMember) {
            return getCertificationsByPubkeyForMember(currencyId, pubkey);
        }
        else {
            return getCertificationsByPubkeyForNonMember(currencyId, uid, pubkey);
        }
    }


    public WotIdentityCertifications getCertifiedBy(long currencyId, String uid) {
        Log.d(TAG, String.format("Try to get certifications done by uid: %s", uid));

        // call certified-by
        String path = String.format(URL_CERTIFIED_BY, uid);
        WotIdentityCertifications result = executeRequest(currencyId, path, WotIdentityCertifications.class);
        
        return result;

    }
    
    public WotIdentityCertifications getCertifiersOf(long currencyId, String uid) {
        Log.d(TAG, String.format("Try to get certifications done to uid: %s", uid));

        // call certifiers-of
        String path = String.format(URL_CERTIFIERS_OF, uid);
        WotIdentityCertifications result = executeRequest(currencyId, path, WotIdentityCertifications.class);
        
        return result;
    }


    public void sendSelf(long currencyId, byte[] pubKey, byte[] secKey, String uid, long timestamp) {
        // http post /wot/add
        HttpPost httpPost = new HttpPost(getPath(currencyId, URL_ADD));

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

        // Execute the request
        executeRequest(httpPost, String.class);
    }

    public String sendCertification(Wallet wallet,
                                    Identity identity) {
        return sendCertification(
                    wallet.getCurrencyId(),
                    wallet.getPubKey(),
                    wallet.getSecKey(),
                    wallet.getIdentity().getUid(),
                    wallet.getIdentity().getTimestamp(),
                    identity.getUid(),
                    identity.getPubkey(),
                    identity.getTimestamp(),
                    identity.getSignature());
    }

    public String sendCertification(long currencyId,
                                    byte[] pubKey, byte[] secKey,
                                  String uid, long timestamp,
                                  String userUid, String userPubKeyHash,
                                  long userTimestamp, String userSignature) {
        // http post /wot/add
        HttpPost httpPost = new HttpPost(getPath(currencyId, URL_ADD));

        // Read the current block (number and hash)
        BlockchainRemoteService blockchainService = ServiceLocator.instance().getBlockchainRemoteService();
        BlockchainBlock currentBlock = blockchainService.getCurrentBlock(currencyId);
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

    public void addAllIdentities(List<Identity> result, WotLookupResults lookupResults, Long currencyId) {
        String currencyName = null;
        if (currencyId != null) {
            currencyName = ServiceLocator.instance().getCurrencyService().getCurrencyNameById(currencyId);
        }

        for (WotLookupResult lookupResult: lookupResults.getResults()) {
            String pubKey = lookupResult.getPubkey();
            for (WotLookupUId source: lookupResult.getUids()) {
                // Create and fill an identity, from a result row
                Identity target = new Identity();
                toIdentity(source, target);

                // fill the pub key
                target.setPubkey(pubKey);

                // Fill currency id and name
                target.setCurrencyId(currencyId);
                target.setCurrency(currencyName);

                result.add(target);
            }
        }
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


    protected Collection<WotCertification> getCertificationsByPubkeyForMember(long currencyId, String pubkey) {

        BlockchainParameters bcParameter = bcService.getParameters(currencyId, true);
        BlockchainBlock currentBlock = bcService.getCurrentBlock(currencyId, true);
        long medianTime = currentBlock.getMedianTime();
        int sigValidity = bcParameter.getSigValidity();
        int sigQty = bcParameter.getSigQty();

        Collection<WotCertification> result = new TreeSet<WotCertification>(ModelUtils.newWotCertificationComparatorByUid());

        // Certifiers of
        WotIdentityCertifications certifiersOfList = getCertifiersOf(currencyId, pubkey);
        boolean certifiersOfIsEmpty = (certifiersOfList == null
                || certifiersOfList.getCertifications() == null);
        int validWrittenCertifiersCount = 0;
        if (!certifiersOfIsEmpty) {
            for (WotCertification certifier : certifiersOfList.getCertifications()) {
                certifier.setCertifiedBy(false);

                // Set the currency Id
                certifier.setCurrencyId(currencyId);

                result.add(certifier);

                long certificationAge = medianTime - certifier.getTimestamp();
                if(certificationAge <= sigValidity) {
                    if (certifier.isMember() && certifier.isWritten()) {
                        validWrittenCertifiersCount++;
                    }
                    certifier.setValid(true);
                }
                else {
                    certifier.setValid(false);
                }
            }
        }

        if (validWrittenCertifiersCount >= sigQty) {
            Log.d(TAG, String.format("pubkey [%s] has %s valid signatures: should be a member", pubkey, validWrittenCertifiersCount));
        }
        else {
            Log.d(TAG, String.format("pubkey [%s] has %s valid signatures: not a member", pubkey, validWrittenCertifiersCount));
        }

        // Certified by
        WotIdentityCertifications certifiedByList = getCertifiedBy(currencyId, pubkey);
        boolean certifiedByIsEmpty = (certifiedByList == null
                || certifiedByList.getCertifications() == null);

        if (!certifiedByIsEmpty) {
            for (WotCertification certifiedBy : certifiedByList.getCertifications()) {

                certifiedBy.setCertifiedBy(true);

                // Set the currency Id
                certifiedBy.setCurrencyId(currencyId);

                result.add(certifiedBy);

                long certificationAge = medianTime - certifiedBy.getTimestamp();
                if(certificationAge <= sigValidity) {
                    certifiedBy.setValid(true);
                }
                else {
                    certifiedBy.setValid(false);
                }
            }
        }

        // Group certifications  by [uid, pubKey] and keep last timestamp
        result = groupByUidAndPubKey(result, true);

        return result;
    }

    protected Collection<WotCertification> getCertificationsByPubkeyForNonMember(long currencyId, final String uid, final String pubkey) {
        // Ordered list, by uid/pubkey/cert time

        Collection<WotCertification> result = new TreeSet<WotCertification>(ModelUtils.newWotCertificationComparatorByUid());

        Log.d(TAG, String.format("Get non member WOT, by uid [%s] and pubKey [%s]", uid, pubkey));

        // call lookup
        String path = String.format(URL_LOOKUP, pubkey);
        WotLookupResults lookupResults = executeRequest(currencyId, path, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId lookupUId = getUidByUidAndPublicKey(lookupResults, uid, pubkey);

        // Read certifiers, if any
        Map<String, WotCertification> certifierByPubkeys = new HashMap<String, WotCertification>();
        if (lookupUId != null && lookupUId.getOthers() != null) {
            for(WotLookupSignature lookupSignature: lookupUId.getOthers()) {
                Collection<WotCertification> certifiers = toCertifierCertifications(lookupSignature, currencyId);
                result.addAll(certifiers);
            }
        }

        // Read certified-by
        if (CollectionUtils.isNotEmpty(lookupResults.getResults())) {
            for (WotLookupResult lookupResult: lookupResults.getResults()) {
                if (lookupResult.getSigned() != null) {
                    for(WotLookupSignature lookupSignature : lookupResult.getSigned()) {
                        WotCertification certifiedBy = toCertifiedByCerticication(lookupSignature);

                        // Set the currency Id
                        certifiedBy.setCurrencyId(currencyId);

                        // If exists, link to other side certification
                        String certifiedByPubkey = certifiedBy.getPubkey();
                        if (certifierByPubkeys.containsKey(certifiedByPubkey)) {
                            WotCertification certified = certifierByPubkeys.get(certifiedByPubkey);
                            certified.setOtherEnd(certifiedBy);
                        }

                        // If only a certifier, just add to the list
                        else {
                            result.add(certifiedBy);
                        }
                    }
                }
            }
        }

        // Group certifications  by [uid, pubKey] and keep last timestamp
        result = groupByUidAndPubKey(result, true);

        return result;
    }

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

    private Collection<WotCertification> toCertifierCertifications(final WotLookupSignature source, final long currencyId) {
        List<WotCertification> result = new ArrayList<WotCertification>();
        // If only one uid
        if (source.getUids().length == 1) {
            WotCertification target = new WotCertification();

            // uid
            target.setUid(source.getUids()[0]);

            // certifier
            target.setCertifiedBy(false);

            // Pubkey
            target.setPubkey(source.getPubkey());

            // Is member
            target.setMember(source.isMember());

            // Set currency Id
            target.setCurrencyId(currencyId);

            result.add(target);
        }
        else {
            for(String uid: source.getUids()) {
                WotCertification target = new WotCertification();

                // uid
                target.setUid(uid);

                // certified by
                target.setCertifiedBy(false);

                // Pubkey
                target.setPubkey(source.getPubkey());

                // Is member
                target.setMember(source.isMember());

                // Set currency Id
                target.setCurrencyId(currencyId);

                result.add(target);
            }
        }
        return result;
    }

    private WotCertification toCertifiedByCerticication(final WotLookupSignature source) {

        WotCertification target = new WotCertification();
        // uid
        target.setUid(source.getUid());

        // certifieb by
        target.setCertifiedBy(true);

        if (source.getMeta() != null) {

            // timestamp
            Integer timestamp = source.getMeta().get(WotLookupSignature.META_KEY_TS);
            if (timestamp != null) {
                target.setTimestamp(timestamp.longValue());
            }
        }

        // Pubkey
        target.setPubkey(source.getPubkey());

        // Is member
        target.setMember(source.isMember());

        // add to result list
        return target;
    }

    /**
     *
     * @param orderedCertifications a list, ordered by uid, pubkey, timestamp (DESC)
     * @return
     */
    private Collection<WotCertification> groupByUidAndPubKey(Collection<WotCertification> orderedCertifications, boolean orderResultByDate) {
        if (CollectionUtils.isEmpty(orderedCertifications)) {
            return orderedCertifications;
        }

        List<WotCertification> result = new ArrayList<WotCertification>();

        StringBuilder keyBuilder = new StringBuilder();
        String previousIdentityKey = null;
        WotCertification previousCert = null;
        for (WotCertification cert : orderedCertifications) {
            String identityKey = keyBuilder.append(cert.getUid())
                    .append("~~")
                    .append(cert.getPubkey())
                    .toString();
            boolean certifiedBy = cert.isCertifiedBy();

            // Seems to be the same identity as previous entry
            if (identityKey.equals(previousIdentityKey)) {

                if (certifiedBy != previousCert.isCertifiedBy()) {
                    // merge with existing other End (if exists)
                    merge(cert, previousCert.getOtherEnd());

                    // previousCert = certifier, so keep it and link the current cert
                    if (!certifiedBy) {
                        previousCert.setOtherEnd(cert);
                    }

                    // previousCert = certified-by, so prefer the current cert
                    else {
                        cert.setOtherEnd(previousCert);
                        previousCert = cert;
                    }
                }

                // Merge
                else {
                    merge(previousCert, cert);
                }
            }

            // if identity changed
            else {
                // So add the previous cert to result
                if (previousCert != null) {
                    result.add(previousCert);
                }

                // And prepare next iteration
                previousIdentityKey = identityKey;
                previousCert = cert;
            }

            // prepare the next loop
            keyBuilder.setLength(0);

        }

        if (previousCert != null) {
            result.add(previousCert);
        }

        if (orderResultByDate) {
            Collections.sort(result, ModelUtils.newWotCertificationComparatorByDate());
        }

        return result;
    }

    private void merge(WotCertification previousCert, WotCertification cert) {
        if (cert != null && cert.getTimestamp() >  previousCert.getTimestamp()) {
            previousCert.setTimestamp(cert.getTimestamp());
        }
    }
}
