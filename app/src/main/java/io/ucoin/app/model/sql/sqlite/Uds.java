package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinUd;
import io.ucoin.app.model.UcoinUds;
import io.ucoin.app.model.http_api.UdHistory;
import io.ucoin.app.sqlite.SQLiteTable;

final public class Uds extends Table
        implements UcoinUds {

    private Long mWalletId;

    public Uds(Context context, Long walletId) {
        this(context, walletId, SQLiteTable.Ud.WALLET_ID + "=?", new String[]{walletId.toString()});
    }

    private Uds(Context context, Long walletId, String selection, String[] selectionArgs) {
        this(context, walletId, selection, selectionArgs, null);
    }

    private Uds(Context context, Long walletId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.UD_URI, selection, selectionArgs, sortOrder);
        mWalletId = walletId;
    }

    @Override
    public UcoinUd add(UdHistory.Ud ud) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.Ud.WALLET_ID, mWalletId);
        values.put(SQLiteTable.Ud.BLOCK, ud.block_number);
        values.put(SQLiteTable.Ud.CONSUMED, ud.consumed);
        values.put(SQLiteTable.Ud.TIME, ud.time);
        values.put(SQLiteTable.Ud.QUANTITATIVE_AMOUNT, ud.amount.toString());

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new Ud(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinUd getById(Long id) {
        return new Ud(mContext, id);
    }

    @Override
    public Iterator<UcoinUd> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinUd> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new Ud(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}