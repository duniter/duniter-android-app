package io.ucoin.app.service;

import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.technical.crypto.CryptoUtils;
import io.ucoin.app.technical.crypto.TestFixtures;

/**
 * Created by eis on 14/01/15.
 */
public class DataService extends BaseService {

    /** Logger. */
    private static final String TAG = "DataService";

    private final boolean isDev = true;

    public DataService() {
        super();
    }

    public Wallet getDefaultWallet() {
        Wallet result;
        byte[] secretKey = null;
        if (isDev) {
            result = new Wallet();
            result.getIdentity().setUid("kimamila");
            result.setSalt("benoit.lavenier@e-is.pro");
        }
        else {
            // TODO : replace from a database access ?
            TestFixtures fixtures = new TestFixtures();

            Identity identity = new Identity();
            identity.setUid(fixtures.getUid());
            identity.setPubkey(fixtures.getUserPublicKey());
            identity.setTimestamp(fixtures.getSelfTimestamp());
            identity.setSignature(fixtures.getSelfSignature());
            result = new Wallet(
                    CryptoUtils.decodeBase58(fixtures.getUserPrivateKey()),
                    identity);
        }

        return result;
    }

}
