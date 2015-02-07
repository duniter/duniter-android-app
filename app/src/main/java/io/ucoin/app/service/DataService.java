package io.ucoin.app.service;

import android.database.Cursor;

import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Currency;
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
                    fixtures.getCurrency(),
                    CryptoUtils.decodeBase58(fixtures.getUserPrivateKey()),
                    identity);
        }

        return result;
    }


    public Currency toCurrency(Cursor cursor) {
        Currency result = new Currency();
        // TODO kimamila: get by index (with increment)
        int idIndex = cursor.getColumnIndex(Contract.Currency._ID);
        result.setId(cursor.getInt(idIndex));

        int currencyNameIndex = cursor.getColumnIndex(Contract.Currency.CURRENCY_NAME);
        result.setCurrencyName(cursor.getString(currencyNameIndex));

        int membersCountIndex = cursor.getColumnIndex(Contract.Currency.MEMBERS_COUNT);
        result.setMembersCount(cursor.getInt(membersCountIndex));

        int firstBlockSignatureIndex = cursor
                .getColumnIndex(Contract.Currency.FIRST_BLOCK_SIGNATURE);
        result.setFirstBlockSignature(cursor.getString(firstBlockSignatureIndex));

        return result;
    }
}
