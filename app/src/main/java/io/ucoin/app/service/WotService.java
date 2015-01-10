package io.ucoin.app.service;

import android.util.Log;


import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.model.*;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.crypto.CryptoUtils;

public class WotService extends AbstractNetworkService {

    private static final String TAG = "WotService";

    public static final String URL_BASE = "/wot";

    public static final String URL_ADD = URL_BASE + "/add";

    public static final String URL_LOOKUP = URL_BASE + "/lookup/%s";

    public static final String URL_CERTIFIED_BY = URL_BASE + "/certified-by/%s";

    public static final String URL_CERTIFIERS_OF = URL_BASE + "/certifiers-of/%s";

    public CryptoService cryptoService;

    public WotService() {
        super();
    }

    @Override
    public void initialize() {
        cryptoService = ServiceLocator.instance().getCryptoService();
    }

    public WotLookupResults find(String uidPattern) {
        Log.d(TAG, String.format("Try to find user info by uid: %s", uidPattern));

        // get parameter
        String path = String.format(URL_LOOKUP, uidPattern);
        HttpGet lookupHttpGet = new HttpGet(getAppendedPath(path));
        WotLookupResults lookupResult = executeRequest(lookupHttpGet, WotLookupResults.class);

        return lookupResult;

    }

    public List<BasicIdentity> toIdentities(WotLookupResults lookupResults) {
        List<BasicIdentity> result = new ArrayList<>();

        for (WotLookupResult lookupResult: lookupResults.getResults()) {
            String pubKey = lookupResult.getPubkey();
            for (WotLookupUId lookupUid: lookupResult.getUids()) {
                String uid = lookupUid.getUid();
                String self = lookupUid.getSelf();

                BasicIdentity identity = new BasicIdentity();
                identity.setPubkey(pubKey);
                identity.setUid(uid);
                identity.setSignature(self);
                result.add(identity);
            }
        }
        return result;
    }

    public WotLookupUId findByUid(String uid) {
        Log.d(TAG, String.format("Try to find user info by uid: %s", uid));

        // call lookup
        String path = String.format(URL_LOOKUP, uid);
        HttpGet lookupHttpGet = new HttpGet(getAppendedPath(path));
        WotLookupResults lookupResults = executeRequest(lookupHttpGet, WotLookupResults.class);

        // Retrieve the exact uid
        WotLookupUId uniqueResult = getUid(lookupResults, uid);
        if (uniqueResult == null) {
            return null;
        }
        
        return uniqueResult;
    }

    public WotIdentityCertifications getCertifiedBy(String uid) {
        Log.d(TAG, String.format("Try to get certifications done by uid: %s", uid));

        // call certified-by
        String path = String.format(URL_CERTIFIED_BY, uid);
        HttpGet httpGet = new HttpGet(getAppendedPath(path));
        WotIdentityCertifications result = executeRequest(httpGet, WotIdentityCertifications.class);
        
        return result;

    }
    
    public WotIdentityCertifications getCertifiersOf(String uid) {
        Log.d(TAG, String.format("Try to get certifications done to uid: %s", uid));

        // call certifiers-of
        String path = String.format(URL_CERTIFIERS_OF, uid);
        HttpGet httpGet = new HttpGet(getAppendedPath(path));
        WotIdentityCertifications result = executeRequest(httpGet, WotIdentityCertifications.class);
        
        return result;

    }

    public String sendSelf(byte[] pubKey, byte[] secKey, String uid) {
        return sendSelf(
                    pubKey,
                    secKey,
                    uid,
                    System.currentTimeMillis());
    }

	public String sendSelf(byte[] pubKey, byte[] secKey, String uid, long timestamp) {
		// http post /wot/add
        HttpPost httpPost = new HttpPost(getAppendedPath(URL_ADD));

        // compute the self-certification
		String selfCertification = getSelfCertification(pubKey, secKey, uid, timestamp);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("pubkey", CryptoUtils.encodeBase58(pubKey)));
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

    /* -- Internal methods -- */

    protected String getSelfCertification(byte[] pubKey, byte[] secretKey, String uid, long timestamp) {
        // Create the self part to sign
        StringBuilder buffer = new StringBuilder()
                .append("UID:")
                .append(uid)
                .append("\nMETA:TS:")
                .append(timestamp)
                .append('\n');

        // Compute the signature
        String signature = cryptoService.sign(buffer.toString(), secretKey);

        // Append the signature
        return buffer.append(signature)
                .append('\n')
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

}
