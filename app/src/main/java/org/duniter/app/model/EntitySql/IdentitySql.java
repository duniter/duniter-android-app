package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntitySql.base.AbstractSql;
import org.duniter.app.services.SqlService;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class IdentitySql extends AbstractSql<Identity> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(IdentityTable.TABLE_NAME+"/").build();
    public static final int CODE = 50;

    public IdentitySql(Context context) {
        super(context,URI);
    }

    public Identity getByWalletId(long id) {
        Identity identity = null;
        Cursor cursor = query(IdentityTable.WALLET_ID+"=?",new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()){
            identity = fromCursor(cursor);
        }
        cursor.close();
        return identity;
    }

    public List<Identity> getAllIdentity() {
        List<Identity> identities = new ArrayList<>();
        Cursor cursor = query(null,null);
        if (cursor.moveToFirst()){
            do {
                identities.add(fromCursor(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return identities;
    }

    public List<Identity> getAllByCurrencyWithWallet(Currency currency) {
        List<Identity> identities = new ArrayList<>();
        Cursor cursor = query(IdentityTable.CURRENCY_ID+"=?",new String[]{String.valueOf(currency.getId())});
        if (cursor.moveToFirst()){
            do {
                Identity identity = fromCursor(cursor);
                identity.setCurrency(currency);
                identity.setWallet(SqlService.getWalletSql(context).getById(identity.getWalletId()));
                identities.add(identity);
            }while (cursor.moveToNext());
        }
        return identities;
    }


    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + IdentityTable.TABLE_NAME + "(" +
                IdentityTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                IdentityTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                IdentityTable.WALLET_ID + INTEGER + UNIQUE + NOTNULL + COMMA +
                IdentityTable.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                IdentityTable.SIG_DATE + INTEGER + COMMA +
                IdentityTable.SELF_BLOCK_UID + TEXT + COMMA +
                IdentityTable.SYNC_BLOCK + INTEGER + NOTNULL + " DEFAULT 0" + COMMA +
                IdentityTable.UID + TEXT + NOTNULL + COMMA +
                "FOREIGN KEY (" + IdentityTable.WALLET_ID + ") REFERENCES " +
                WalletSql.WalletTable.TABLE_NAME + "(" + WalletSql.WalletTable._ID + ") ON DELETE CASCADE" + COMMA +
                "FOREIGN KEY (" + IdentityTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE" +
                ")";
    }

    @Override
    public Identity fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(IdentityTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(IdentityTable.CURRENCY_ID);
        int walletIdIndex = cursor.getColumnIndex(IdentityTable.WALLET_ID);
        int publicKeyIndex = cursor.getColumnIndex(IdentityTable.PUBLIC_KEY);
        int uidIndex = cursor.getColumnIndex(IdentityTable.UID);
        int syncBlockIndex = cursor.getColumnIndex(IdentityTable.SYNC_BLOCK);
        int sigDateIndex = cursor.getColumnIndex(IdentityTable.SIG_DATE);
        int selfBlockUidIndex = cursor.getColumnIndex(IdentityTable.SELF_BLOCK_UID);

        Identity identity = new Identity();
        identity.setId(cursor.getLong(idIndex));
        identity.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        identity.setUid(cursor.getString(uidIndex));
        identity.setWallet(new Wallet(cursor.getLong(walletIdIndex)));
        identity.setPublicKey(cursor.getString(publicKeyIndex));
        identity.setSyncBlock(cursor.getLong(syncBlockIndex));
        identity.setSigDate(cursor.getLong(sigDateIndex));
        identity.setSelfBlockUid(cursor.getString(selfBlockUidIndex));
        return identity;
    }

    @Override
    public ContentValues toContentValues(Identity entity) {
        ContentValues values = new ContentValues();
        values.put(IdentityTable.CURRENCY_ID, entity.getCurrencyId());
        values.put(IdentityTable.WALLET_ID, entity.getWalletId());
        values.put(IdentityTable.PUBLIC_KEY, entity.getPublicKey());
        values.put(IdentityTable.UID, entity.getUid());
        values.put(IdentityTable.SYNC_BLOCK, entity.getSyncBlock());
        values.put(IdentityTable.SIG_DATE, entity.getSigDate());
        values.put(IdentityTable.SELF_BLOCK_UID, entity.getSelfBlockUid());
        return values;
    }

    public class IdentityTable implements BaseColumns {
        public static final String TABLE_NAME = "identity";

        public static final String CURRENCY_ID = "currency_id";
        public static final String WALLET_ID = "wallet_id";
        public static final String PUBLIC_KEY = "public_key";
        public static final String UID = "uid";
        public static final String SIG_DATE = "sig_date";
        public static final String SYNC_BLOCK = "sync_block";
        public static final String SELF_BLOCK_UID = "self_block_uid";
    }
}
