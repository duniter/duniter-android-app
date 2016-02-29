package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.BlockchainParameter;
import io.ucoin.app.model.http_api.NetworkPeering;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Currencies extends Table
        implements UcoinCurrencies {

    public Currencies(Context context) {
        this(context, null, null, null);
    }

    private Currencies(Context context, String selection, String[] selectionArgs) {
        this(context, selection, selectionArgs, null);
    }

    private Currencies(Context context, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.CURRENCY_URI, selection, selectionArgs, sortOrder);
    }

    @Override
    public UcoinCurrency add(BlockchainParameter parameter, NetworkPeering networkPeering) {
        ContentValues values = new ContentValues();

        values.put(SQLiteTable.Currency.NAME, parameter.currency);
        values.put(SQLiteTable.Currency.C, parameter.c);
        values.put(SQLiteTable.Currency.DT, parameter.dt);
        values.put(SQLiteTable.Currency.UD0, parameter.ud0);
        values.put(SQLiteTable.Currency.SIGDELAY, parameter.sigDelay);
        values.put(SQLiteTable.Currency.SIGVALIDITY, parameter.sigValidity);
        values.put(SQLiteTable.Currency.SIGQTY, parameter.sigQty);
        values.put(SQLiteTable.Currency.SIGWOT, parameter.sigWoT);
        values.put(SQLiteTable.Currency.MSVALIDITY, parameter.msValidity);
        values.put(SQLiteTable.Currency.STEPMAX, parameter.stepMax);
        values.put(SQLiteTable.Currency.MEDIANTIMEBLOCKS, parameter.medianTimeBlocks);
        values.put(SQLiteTable.Currency.AVGGENTIME, parameter.avgGenTime);
        values.put(SQLiteTable.Currency.DTDIFFEVAL, parameter.dtDiffEval);
        values.put(SQLiteTable.Currency.BLOCKSROT, parameter.blocksRot);
        values.put(SQLiteTable.Currency.PERCENTROT, parameter.percentRot);

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            UcoinCurrency currency = new Currency(mContext, Long.parseLong(uri.getLastPathSegment()));
            currency.peers().add(networkPeering);
            return currency;
        } else {
            return null;
        }
    }

    @Override
    public UcoinCurrency getById(Long id) {
        String selection = SQLiteTable.Currency._ID + "=?";
        String[] selectionArgs = new String[]{id.toString()};
        UcoinCurrencies currencies = new Currencies(mContext, selection, selectionArgs);
        if (currencies.iterator().hasNext()) {
            return currencies.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public UcoinCurrency getByName(String name) {
        String selection = SQLiteTable.Currency.NAME + "=?";
        String[] selectionArgs = new String[]{name};
        UcoinCurrencies currencies = new Currencies(mContext, selection, selectionArgs);
        if (currencies.iterator().hasNext()) {
            return currencies.iterator().next();
        } else {
            return null;
        }
    }

    @Override
    public Cursor getAll() {
        return fetch();
    }

    @Override
    public ArrayList<UcoinCurrency> list() {
        Cursor cursor = fetch();
        ArrayList<UcoinCurrency> data = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Currency(mContext, id));
            }
            cursor.close();
        }
        return data;
    }

    @Override
    public Iterator<UcoinCurrency> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinCurrency> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Currency(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}