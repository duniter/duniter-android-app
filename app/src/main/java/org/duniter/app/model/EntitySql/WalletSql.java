package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class WalletSql extends AbstractSql<Wallet> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(WalletTable.TABLE_NAME+"/").build();
    public static final int CODE = 90;

    public WalletSql(Context context) {
        super(context,URI);
    }

    public List<Wallet> getAllWallet() {
        List<Wallet> walletList = new ArrayList<>();
        Cursor cursor = query(null,null);
        if (cursor.moveToFirst()){
            do {
                walletList.add(fromCursor(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return walletList;
    }

    public List<Wallet> getByCurrency(Currency currency) {
        List<Wallet> result = new ArrayList<>();
        Cursor cursor = query(WalletTable.CURRENCY_ID + "=?",new String[]{String.valueOf(currency.getId())});
        if (cursor.moveToFirst()){
            do {
                Wallet wallet = fromCursor(cursor);
                wallet.setCurrency(currency);
                result.add(wallet);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public List<Wallet> getIfIdentity() {
        List<Wallet> result = new ArrayList<>();
        Cursor cursor = query(WalletTable.HAVE_IDENTITY + "=?",new String[]{"true"});
        if (cursor.moveToFirst()){
            do {
                Wallet wallet = fromCursor(cursor);
                result.add(wallet);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }


    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + WalletTable.TABLE_NAME + "(" +
                WalletTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                WalletTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                WalletTable.HAVE_IDENTITY + TEXT + NOTNULL + " DEFAULT \"false\" " + COMMA +
                WalletTable.SALT + TEXT + NOTNULL + COMMA +
                WalletTable.PUBLIC_KEY + TEXT + NOTNULL + COMMA +
                WalletTable.PRIVATE_KEY + TEXT + COMMA +
                WalletTable.ALIAS + TEXT + COMMA +
                WalletTable.AMOUNT + TEXT + NOTNULL + " DEFAULT \"0\" " + COMMA +
                WalletTable.SYNC_BLOCK + INTEGER + NOTNULL + " DEFAULT 0 " + COMMA +
                "FOREIGN KEY (" + WalletTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE" + COMMA +
                UNIQUE + "(" + WalletTable.CURRENCY_ID + COMMA + WalletTable.PUBLIC_KEY + ")" +
                ")";
    }

    @Override
    public Wallet fromCursor(Cursor cursor) {

        int idIndex = cursor.getColumnIndex(WalletTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(WalletTable.CURRENCY_ID);
        int haveIdentityIndex = cursor.getColumnIndex(WalletTable.HAVE_IDENTITY);
        int saltIndex = cursor.getColumnIndex(WalletTable.SALT);
        int publicKeyIndex = cursor.getColumnIndex(WalletTable.PUBLIC_KEY);
        int privaetKeyIndex = cursor.getColumnIndex(WalletTable.PRIVATE_KEY);
        int aliasIndex = cursor.getColumnIndex(WalletTable.ALIAS);
        int syncBlockIndex = cursor.getColumnIndex(WalletTable.SYNC_BLOCK);
        int amountIndex = cursor.getColumnIndex(WalletTable.AMOUNT);

        Wallet wallet = new Wallet();
        wallet.setId(cursor.getLong(idIndex));
        wallet.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        wallet.setHaveIdentity(cursor.getString(haveIdentityIndex).equals("true"));
        wallet.setSalt(cursor.getString(saltIndex));
        wallet.setPublicKey(cursor.getString(publicKeyIndex));
        wallet.setPrivateKey(cursor.getString(privaetKeyIndex));
        wallet.setAlias(cursor.getString(aliasIndex));
        wallet.setSyncBlock(cursor.getLong(syncBlockIndex));
        wallet.setAmount(new BigInteger(cursor.getString(amountIndex)));

        return wallet;
    }

    @Override
    public ContentValues toContentValues(Wallet entity) {
        ContentValues values = new ContentValues();
        values.put(WalletTable.CURRENCY_ID, entity.getCurrencyId());
        values.put(WalletTable.HAVE_IDENTITY, String.valueOf(entity.getHaveIdentity()));
        values.put(WalletTable.SALT, entity.getSalt());
        values.put(WalletTable.ALIAS, entity.getAlias());
        values.put(WalletTable.PUBLIC_KEY, entity.getPublicKey());
        values.put(WalletTable.PRIVATE_KEY, entity.getPrivateKey());
        values.put(WalletTable.SYNC_BLOCK, entity.getSyncBlock());
        values.put(WalletTable.AMOUNT, entity.getAmount().toString());
        return values;
    }

    public class WalletTable implements BaseColumns {
        public static final String TABLE_NAME = "wallet";

        public static final String CURRENCY_ID = "currency_id";
        public static final String HAVE_IDENTITY = "have_identity";
        public static final String SALT = "salt";
        public static final String PUBLIC_KEY = "public_key";
        public static final String PRIVATE_KEY = "private_key";
        public static final String ALIAS = "alias";
        public static final String SYNC_BLOCK = "sync_block";
        public static final String AMOUNT = "amount";
    }
}
