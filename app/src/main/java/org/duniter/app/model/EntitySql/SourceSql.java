package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.provider.BaseColumns;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Source;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 20/04/16.
 */
public class SourceSql extends AbstractSql<Source> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(SourceTable.TABLE_NAME+"/").build();
    public static final int CODE = 100;


    public SourceSql(Context context) {
        super(context,URI);
    }

    public BigInteger insertList(List<Source> list, long walletId, BigInteger amount){
        List<Source> listSql = getByWallet(walletId);

        for (Source s:listSql){
            if (!list.contains(s)){
                delete(s.getId());
                amount = amount.subtract(s.getAmount());
            }
        }

        for (Source s : list){
            if (!listSql.contains(s)){
                try {
                    insert(s);
                }catch (SQLiteConstraintException e){

                }
                amount = amount.add(s.getAmount());
            }
        }
        return amount;
    }

    public List<Source> getByWallet(long id) {
        List<Source> sources = new ArrayList<>();
        Cursor cursor = query(SourceTable.WALLET_ID+"=?",new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()){
            do {
                sources.add(fromCursor(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return sources;
    }

    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + SourceTable.TABLE_NAME + "(" +
                SourceTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                SourceTable.CURRENCY_ID + INTEGER + NOTNULL + COMMA +
                SourceTable.WALLET_ID + INTEGER + NOTNULL + COMMA +
                SourceTable.STATE + TEXT + NOTNULL + COMMA +
                SourceTable.AMOUNT + TEXT + NOTNULL + " DEFAULT \"0\"" + COMMA +
                SourceTable.IDENTIFIER + TEXT + COMMA +
                SourceTable.NOFFSET + INTEGER + COMMA +
                SourceTable.TYPE + TEXT + NOTNULL + COMMA +
                "FOREIGN KEY (" + SourceTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE" + COMMA +
                "FOREIGN KEY (" + SourceTable.WALLET_ID + ") REFERENCES " +
                WalletSql.WalletTable.TABLE_NAME + "(" + WalletSql.WalletTable._ID + ") ON DELETE CASCADE" +
                ")";
    }

    @Override
    public Source fromCursor(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(SourceTable._ID);
        int currencyIdIndex = cursor.getColumnIndex(SourceTable.CURRENCY_ID);
        int walletIdIndex = cursor.getColumnIndex(SourceTable.WALLET_ID);
        int stateIndex = cursor.getColumnIndex(SourceTable.STATE);
        int amountIndex = cursor.getColumnIndex(SourceTable.AMOUNT);
        int identifierIndex = cursor.getColumnIndex(SourceTable.IDENTIFIER);
        int noffsetIndex = cursor.getColumnIndex(SourceTable.NOFFSET);
        int typeIndex = cursor.getColumnIndex(SourceTable.TYPE);

        Source source = new Source();
        source.setId(cursor.getLong(idIndex));
        source.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        source.setWallet(new Wallet(cursor.getLong(walletIdIndex)));
        source.setState(cursor.getString(stateIndex));
        source.setAmount(new BigInteger(cursor.getString(amountIndex)));
        source.setIdentifier(cursor.getString(identifierIndex));
        source.setNoffset(cursor.getInt(noffsetIndex));
        source.setType(cursor.getString(typeIndex));

        return source;
    }

    @Override
    public ContentValues toContentValues(Source entity) {
        ContentValues values = new ContentValues();
        values.put(SourceTable.CURRENCY_ID, entity.getCurrency().getId());
        values.put(SourceTable.WALLET_ID, entity.getWallet().getId());
        values.put(SourceTable.AMOUNT, entity.getAmount().toString());
        values.put(SourceTable.STATE, entity.getState());
        values.put(SourceTable.IDENTIFIER, entity.getIdentifier());
        values.put(SourceTable.NOFFSET, entity.getNoffset());
        values.put(SourceTable.TYPE, entity.getType());
        return values;
    }

    public class SourceTable implements BaseColumns {
        public static final String TABLE_NAME = "source";

        public static final String CURRENCY_ID = "currency_id";
        public static final String WALLET_ID = "wallet_id";
        public static final String AMOUNT = "amount";
        public static final String STATE = "state";
        public static final String IDENTIFIER = "identifier";
        public static final String NOFFSET = "noffset";
        public static final String TYPE = "type";
    }
}
