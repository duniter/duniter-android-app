package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.BlockUd;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class BlockUdSql extends AbstractSql<BlockUd> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(BlockTable.TABLE_NAME+"/").build();
    public static final int CODE = 20;


    public BlockUdSql(Context context) {
        super(context,URI);
    }

    public List<Integer> getListNumber(long currencyId) {
        Cursor cursor = query(BlockTable.CURRENCY_ID+"=?",new String[]{String.valueOf(currencyId)});
        List<Integer> result = new ArrayList<>();
        if (cursor.moveToFirst()){
            do {
                int val = cursor.getInt(cursor.getColumnIndex(BlockTable.NUMBER));
                result.add(val);
            }while (cursor.moveToNext());
            cursor.close();
        }
        return result;
    }

    public BlockUd last(Long currencyId) {
        BlockUd blockUd = null;
        Cursor cursor = query(BlockTable.CURRENCY_ID+"=?",new String[]{String.valueOf(currencyId)},BlockTable.NUMBER + " DESC");
        if (cursor.moveToFirst()){
            blockUd = fromCursor(cursor);
        }
        cursor.close();
        return blockUd;
    }

    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + BlockTable.TABLE_NAME + "(" +
                BlockTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                BlockTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                BlockTable.NUMBER + INTEGER + NOTNULL + COMMA +
                BlockTable.MEDIAN_TIME + INTEGER + NOTNULL + COMMA +
                BlockTable.DIVIDEND + TEXT + COMMA +
                BlockTable.MONETARY_MASS + TEXT + NOTNULL + COMMA +
                BlockTable.MEMBERS_COUNT + INTEGER + NOTNULL + COMMA +
                BlockTable.HASH + INTEGER + NOTNULL + COMMA +
                "FOREIGN KEY (" + BlockTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ")" + COMMA +
                UNIQUE + "(" + BlockTable.CURRENCY_ID + COMMA + BlockTable.NUMBER + ")" +
                ")";
    }

    @Override
    public BlockUd fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(BlockTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(BlockTable.CURRENCY_ID);
        int numberIndex = cursor.getColumnIndex(BlockTable.NUMBER);
        int medianTimeIndex = cursor.getColumnIndex(BlockTable.MEDIAN_TIME);
        int membersCountIndex = cursor.getColumnIndex(BlockTable.MEMBERS_COUNT);
        int monetaryMassIndex = cursor.getColumnIndex(BlockTable.MONETARY_MASS);
        int hashIndex = cursor.getColumnIndex(BlockTable.HASH);
        int dividendIndex = cursor.getColumnIndex(BlockTable.DIVIDEND);

        BlockUd blockUd =new BlockUd();
        blockUd.setId(cursor.getLong(idIndex));
        blockUd.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        blockUd.setNumber(cursor.getLong(numberIndex));
        blockUd.setMedianTime(cursor.getLong(medianTimeIndex));
        blockUd.setMembersCount(cursor.getLong(membersCountIndex));
        blockUd.setMonetaryMass(new BigInteger(cursor.getString(monetaryMassIndex)));
        blockUd.setHash(cursor.getString(hashIndex));
        blockUd.setDividend(new BigInteger(cursor.getString(dividendIndex)));

        return blockUd;
    }

    @Override
    public ContentValues toContentValues(BlockUd entity) {
        ContentValues values = new ContentValues();
        
        values.put(BlockTable.CURRENCY_ID, entity.getCurrency().getId());
        values.put(BlockTable.NUMBER, entity.getNumber());
        values.put(BlockTable.MONETARY_MASS, entity.getMonetaryMass().toString());
        values.put(BlockTable.MEDIAN_TIME, entity.getMedianTime());
        values.put(BlockTable.MEMBERS_COUNT, entity.getMembersCount());
        values.put(BlockTable.DIVIDEND, entity.getDividend().toString());
        values.put(BlockTable.HASH, entity.getHash());

        return values;
    }

    public class BlockTable implements BaseColumns{
        public static final String TABLE_NAME = "block_ud";

        public static final String CURRENCY_ID = "currency_id";
        public static final String NUMBER = "number";
        public static final String MEDIAN_TIME = "median_time";
        public static final String DIVIDEND = "dividend";
        public static final String MONETARY_MASS = "monetary_mass";
        public static final String MEMBERS_COUNT = "members_count";
        public static final String HASH = "hash";
    }
}
