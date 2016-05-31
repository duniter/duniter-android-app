package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duniter.app.enumeration.TxState;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class TxSql extends AbstractSql<Tx> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(TxTable.TABLE_NAME+"/").build();
    public static final int CODE = 80;


    public TxSql(Context context) {
        super(context,URI);
    }

    public void insertList(List<Tx> list){
        for (Tx tx: list){
            insert(tx);
        }
    }

    public Map<String,Tx> getTxMap(long id) {
        Map<String,Tx> result = new HashMap<>();
        Cursor cursor = query(TxTable.WALLET_ID+"=?",new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()){
            do {
                Tx tx = fromCursor(cursor);
                result.put(tx.getHash(),tx);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public BigInteger getPendingAmount(long id) {
        BigInteger result = BigInteger.ZERO;
        Cursor cursor = query(TxTable.WALLET_ID+"=? AND "+TxTable.STATE+"=?",new String[]{String.valueOf(id), TxState.PENDING.name()});
        if (cursor.moveToFirst()){
            do {
                Tx tx = fromCursor(cursor);
                result.add(tx.getAmount());
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public List<Tx> getPendingTx(long id) {
        List<Tx> result = new ArrayList<>();
        Cursor cursor = query(TxTable.WALLET_ID+"=? AND "+TxTable.STATE+"=?",new String[]{String.valueOf(id), TxState.PENDING.name()});
        if (cursor.moveToFirst()){
            do {
                Tx tx = fromCursor(cursor);
                result.add(tx);
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
        return "CREATE TABLE " + TxTable.TABLE_NAME + "(" +
                TxTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                TxTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                TxTable.WALLET_ID + INTEGER + NOTNULL + COMMA +
                TxTable.STATE + TEXT + NOTNULL + COMMA +
                TxTable.AMOUNT + TEXT + NOTNULL + " DEFAULT \"0\"" + COMMA +
                TxTable.PUBLIC_KEY + TEXT + NOTNULL + " DEFAULT \"UNKNOWN\"" + COMMA +
                TxTable.UID + TEXT + COMMA +
                TxTable.TIME + INTEGER + COMMA +
                TxTable.BLOCK_NUMBER + INTEGER + COMMA +
                TxTable.COMMENT + TEXT + COMMA +
                TxTable.ENC + TEXT + COMMA +
                TxTable.HASH + TEXT + COMMA +
                TxTable.LOCKTIME + INTEGER + NOTNULL + " DEFAULT 0" + COMMA +
                "FOREIGN KEY (" + TxTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE" + COMMA +
                "FOREIGN KEY (" + TxTable.WALLET_ID + ") REFERENCES " +
                WalletSql.WalletTable.TABLE_NAME + "(" + WalletSql.WalletTable._ID + ") ON DELETE CASCADE" + COMMA +
                UNIQUE + "(" + TxTable.HASH + COMMA + TxTable.WALLET_ID + ")" +
                ")";
    }

    @Override
    public Tx fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(TxTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(TxTable.CURRENCY_ID);
        int walletIdIndex = cursor.getColumnIndex(TxTable.WALLET_ID);
        int stateIndex = cursor.getColumnIndex(TxTable.STATE);
        int amountIndex = cursor.getColumnIndex(TxTable.AMOUNT);
        int publicKeyIndex = cursor.getColumnIndex(TxTable.PUBLIC_KEY);
        int uidIndex = cursor.getColumnIndex(TxTable.UID);
        int timeIndex = cursor.getColumnIndex(TxTable.TIME);
        int blockNumberIndex = cursor.getColumnIndex(TxTable.BLOCK_NUMBER);
        int commentIndex = cursor.getColumnIndex(TxTable.COMMENT);
        int encIndex = cursor.getColumnIndex(TxTable.ENC);
        int hashIndex = cursor.getColumnIndex(TxTable.HASH);
        int locktimeIndex = cursor.getColumnIndex(TxTable.LOCKTIME);

        Tx tx = new Tx();
        tx.setId(cursor.getLong(idIndex));
        tx.setWallet(new Wallet(cursor.getLong(walletIdIndex)));
        tx.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        tx.setState(cursor.getString(stateIndex));
        tx.setAmount(new BigInteger(cursor.getString(amountIndex)));
        tx.setPublicKey(cursor.getString(publicKeyIndex));
        tx.setUid(cursor.getString(uidIndex));
        tx.setTime(cursor.getLong(timeIndex));
        tx.setBlockNumber(cursor.getLong(blockNumberIndex));
        tx.setComment(cursor.getString(commentIndex));
        tx.setEnc(Boolean.getBoolean(cursor.getString(encIndex)));
        tx.setHash(cursor.getString(hashIndex));
        tx.setLocktime(cursor.getLong(locktimeIndex));

        return tx;
    }

    @Override
    public ContentValues toContentValues(Tx entity) {
        ContentValues values = new ContentValues();
        values.put(TxTable.CURRENCY_ID, entity.getCurrency().getId());
        values.put(TxTable.WALLET_ID, entity.getWallet().getId());
        values.put(TxTable.STATE,entity.getState());
        values.put(TxTable.AMOUNT, entity.getAmount().toString());
        values.put(TxTable.PUBLIC_KEY, entity.getPublicKey());
        values.put(TxTable.UID, entity.getUid());
        values.put(TxTable.TIME, entity.getTime());
        values.put(TxTable.BLOCK_NUMBER, entity.getBlockNumber());
        values.put(TxTable.COMMENT, entity.getComment());
        values.put(TxTable.ENC, String.valueOf(entity.isEnc()));
        values.put(TxTable.HASH, entity.getHash());
        values.put(TxTable.LOCKTIME, entity.getLocktime());
        return values;
    }

    public class TxTable implements BaseColumns {
        public static final String TABLE_NAME = "tx";

        public static final String CURRENCY_ID = "currency_id";
        public static final String WALLET_ID = "wallet_id";
        public static final String STATE = "state";
        public static final String AMOUNT = "amount";
        public static final String PUBLIC_KEY = "public_Key";
        public static final String UID = "uid";
        public static final String TIME = "time";
        public static final String BLOCK_NUMBER = "block_number";
        public static final String COMMENT = "comment";
        public static final String ENC = "enc";
        public static final String HASH = "hash";
        public static final String LOCKTIME = "locktime";
    }
}
