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

    public List<Source> getMinBaseSourceByWallet(Wallet wallet) {
        List<Source> sources = new ArrayList<>();
        int base = wallet.getBase() == 0 ? 0 : wallet.getBase()-1;
        Cursor cursor = query(
                SourceTable.WALLET_ID+"=? AND "+SourceTable.BASE+"=? AND ("+SourceTable.AMOUNT+"%10)!=0",
                new String[]{
                        String.valueOf(wallet.getId()),
                        String.valueOf(wallet.getBase()-1)});
        if (cursor.moveToFirst()){
            do {
                sources.add(fromCursor(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return sources;
    }

    public List<Source> getByWallet(long id) {
        List<Source> sources = new ArrayList<>();
        Cursor cursor = query(SourceTable.WALLET_ID+"=?",new String[]{String.valueOf(id)},SourceTable.BASE+" ASC");
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
                SourceTable.AMOUNT + INTEGER + NOTNULL + COMMA +
                SourceTable.BASE + INTEGER + NOTNULL + COMMA +
                SourceTable.IDENTIFIER + TEXT + COMMA +
                SourceTable.NOFFSET + INTEGER + COMMA +
                SourceTable.TYPE + TEXT + NOTNULL + COMMA +
                "FOREIGN KEY (" + SourceTable.WALLET_ID + ") REFERENCES " +
                WalletSql.WalletTable.TABLE_NAME + "(" + WalletSql.WalletTable._ID + ") ON DELETE CASCADE" + COMMA +
                "FOREIGN KEY (" + SourceTable.CURRENCY_ID + ") REFERENCES " +
                CurrencySql.CurrencyTable.TABLE_NAME + "(" + CurrencySql.CurrencyTable._ID + ") ON DELETE CASCADE" +
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
        int baseIndex = cursor.getColumnIndex(SourceTable.BASE);

        Source source = new Source();
        source.setId(cursor.getLong(idIndex));
        source.setCurrency(new Currency(cursor.getLong(currencyIdIndex)));
        source.setWallet(new Wallet(cursor.getLong(walletIdIndex)));
        source.setState(cursor.getString(stateIndex));
        source.setAmount(cursor.getLong(amountIndex));
        source.setIdentifier(cursor.getString(identifierIndex));
        source.setNoffset(cursor.getInt(noffsetIndex));
        source.setType(cursor.getString(typeIndex));
        source.setBase(cursor.getInt(baseIndex));

        return source;
    }

    @Override
    public ContentValues toContentValues(Source entity) {
        ContentValues values = new ContentValues();
        values.put(SourceTable.CURRENCY_ID, entity.getCurrency().getId());
        values.put(SourceTable.WALLET_ID, entity.getWallet().getId());
        values.put(SourceTable.AMOUNT, entity.getAmount());
        values.put(SourceTable.BASE, entity.getBase());
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
        public static final String BASE = "base";
        public static final String STATE = "state";
        public static final String IDENTIFIER = "identifier";
        public static final String NOFFSET = "noffset";
        public static final String TYPE = "type";
    }
}
