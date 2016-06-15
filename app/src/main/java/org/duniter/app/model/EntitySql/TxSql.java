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
import org.duniter.app.technical.format.UnitCurrency;

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

    public long getWalletTime(long walletId) {
        long timeAmount = 0;
        String requete = "SELECT sum("+TxTable.AMOUNT_TIME_ORIGIN+") FROM "+TxTable.TABLE_NAME+" WHERE "+TxTable.WALLET_ID + "="+walletId+" AND "+TxTable.IS_UD+"=\"false\"";
        Cursor cursor = query(requete);
        if (cursor.moveToFirst()){
            timeAmount = cursor.getLong(0);
        }
        cursor.close();
        return timeAmount;
    }

    public Map<String,Tx> getTxMap(long id) {
        Map<String,Tx> result = new HashMap<>();
        Cursor cursor = query(TxTable.WALLET_ID+"=? AND "+TxTable.IS_UD+"=?",new String[]{String.valueOf(id),String.valueOf(false)});
        if (cursor.moveToFirst()){
            do {
                Tx tx = fromCursor(cursor);
                result.put(tx.getHash(),tx);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public Map<Long,Tx> getUdMap(long id) {
        Map<Long,Tx> result = new HashMap<>();
        Cursor cursor = query(TxTable.WALLET_ID+"=? AND "+TxTable.IS_UD+"=?",new String[]{String.valueOf(id),String.valueOf(true)});
        if (cursor.moveToFirst()){
            do {
                Tx tx = fromCursor(cursor);
                result.put(tx.getBlockNumber(),tx);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return result;
    }

    public long getPendingAmount(long id) {
        long result = 0;
        Cursor cursor = query(TxTable.WALLET_ID+"=? AND "+TxTable.STATE+"=?",new String[]{String.valueOf(id), TxState.PENDING.name()});
        if (cursor.moveToFirst()){
            do {
                Tx tx = fromCursor(cursor);
                result += tx.getAmount();
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
                TxTable.AMOUNT + INTEGER + NOTNULL + COMMA +
                TxTable.BASE + INTEGER + NOTNULL + COMMA +
                TxTable.AMOUNT_TIME_ORIGIN + INTEGER + NOTNULL + COMMA +
                TxTable.AMOUNT_RELATIF_ORIGIN + REAL + NOTNULL + COMMA +
                TxTable.PUBLIC_KEY + TEXT + NOTNULL + " DEFAULT \"UNKNOWN\"" + COMMA +
                TxTable.IS_UD + TEXT + NOTNULL + COMMA +
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
                UNIQUE + "(" + TxTable.HASH + COMMA + TxTable.WALLET_ID + COMMA + TxTable.BLOCK_NUMBER + COMMA + TxTable.AMOUNT + ")" +
                ")";
    }

    @Override
    public Tx fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(TxTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(TxTable.CURRENCY_ID);
        int walletIdIndex = cursor.getColumnIndex(TxTable.WALLET_ID);
        int stateIndex = cursor.getColumnIndex(TxTable.STATE);
        int amountIndex = cursor.getColumnIndex(TxTable.AMOUNT);
        int baseIndex =cursor.getColumnIndex(TxTable.BASE);
        int amountTimeOriginIndex = cursor.getColumnIndex(TxTable.AMOUNT_TIME_ORIGIN);
        int amountRelatifOriginIndex = cursor.getColumnIndex(TxTable.AMOUNT_RELATIF_ORIGIN);
        int publicKeyIndex = cursor.getColumnIndex(TxTable.PUBLIC_KEY);
        int uidIndex = cursor.getColumnIndex(TxTable.UID);
        int timeIndex = cursor.getColumnIndex(TxTable.TIME);
        int blockNumberIndex = cursor.getColumnIndex(TxTable.BLOCK_NUMBER);
        int commentIndex = cursor.getColumnIndex(TxTable.COMMENT);
        int encIndex = cursor.getColumnIndex(TxTable.ENC);
        int hashIndex = cursor.getColumnIndex(TxTable.HASH);
        int locktimeIndex = cursor.getColumnIndex(TxTable.LOCKTIME);
        int isUdIndex = cursor.getColumnIndex(TxTable.IS_UD);

        Tx tx = new Tx();
        tx.setId(cursor.getLong(idIndex));
        tx.setWallet(new Wallet(cursor.getLong(walletIdIndex)));
        tx.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        tx.setState(cursor.getString(stateIndex));
        tx.setAmount(cursor.getLong(amountIndex));
        tx.setBase(cursor.getInt(baseIndex));
        tx.setAmountTimeOrigin(cursor.getLong(amountTimeOriginIndex));
        tx.setAmountRelatifOrigin(cursor.getDouble(amountRelatifOriginIndex));
        tx.setPublicKey(cursor.getString(publicKeyIndex));
        tx.setUid(cursor.getString(uidIndex));
        tx.setTime(cursor.getLong(timeIndex));
        tx.setBlockNumber(cursor.getLong(blockNumberIndex));
        tx.setComment(cursor.getString(commentIndex));
        tx.setEnc(Boolean.getBoolean(cursor.getString(encIndex)));
        tx.setHash(cursor.getString(hashIndex));
        tx.setLocktime(cursor.getLong(locktimeIndex));
        tx.setUd(Boolean.valueOf(cursor.getString(isUdIndex)));

        return tx;
    }

    @Override
    public ContentValues toContentValues(Tx entity) {
        ContentValues values = new ContentValues();
        values.put(TxTable.CURRENCY_ID, entity.getCurrency().getId());
        values.put(TxTable.WALLET_ID, entity.getWallet().getId());
        values.put(TxTable.STATE,entity.getState());
        values.put(TxTable.AMOUNT, entity.getAmount());
        values.put(TxTable.BASE, entity.getBase());
        values.put(TxTable.AMOUNT_TIME_ORIGIN, entity.getAmountTimeOrigin());
        values.put(TxTable.AMOUNT_RELATIF_ORIGIN, entity.getAmountRelatifOrigin());
        values.put(TxTable.PUBLIC_KEY, entity.getPublicKey());
        values.put(TxTable.UID, entity.getUid());
        values.put(TxTable.TIME, entity.getTime());
        values.put(TxTable.BLOCK_NUMBER, entity.getBlockNumber());
        values.put(TxTable.COMMENT, entity.getComment());
        values.put(TxTable.ENC, String.valueOf(entity.isEnc()));
        values.put(TxTable.HASH, entity.getHash());
        values.put(TxTable.LOCKTIME, entity.getLocktime());
        values.put(TxTable.IS_UD, String.valueOf(entity.getUd()));
        return values;
    }

    public class TxTable implements BaseColumns {
        public static final String TABLE_NAME = "tx";

        public static final String CURRENCY_ID = "currency_id";
        public static final String WALLET_ID = "wallet_id";
        public static final String STATE = "state";
        public static final String AMOUNT = "amount";
        public static final String BASE = "base";
        public static final String AMOUNT_TIME_ORIGIN = "amount_time_origin";
        public static final String AMOUNT_RELATIF_ORIGIN = "amount_relatif_origin";
        public static final String PUBLIC_KEY = "public_Key";
        public static final String UID = "uid";
        public static final String TIME = "time";
        public static final String BLOCK_NUMBER = "block_number";
        public static final String COMMENT = "comment";
        public static final String ENC = "enc";
        public static final String HASH = "hash";
        public static final String LOCKTIME = "locktime";
        public static final String IS_UD = "is_ud";
    }
}
