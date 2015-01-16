package io.ucoin.app.service;

import io.ucoin.app.model.BlockchainParameter;
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

    public DataService() {
        super();
    }

    public Wallet getDefaultWallet() {
        // TODO : replace from a database access ?
        TestFixtures fixtures = new TestFixtures();

        Identity identity = new Identity();
        identity.setUid(fixtures.getUid());
        identity.setPubkey(fixtures.getUserPublicKey());
        identity.setTimestamp(fixtures.getSelfTimestamp());
        identity.setSignature(fixtures.getSelfSignature());
        Wallet result = new Wallet(
                CryptoUtils.decodeBase58(fixtures.getUserPrivateKey()),
                identity);

        return result;
    }

}
