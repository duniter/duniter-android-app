package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinTxIssuer;
import io.ucoin.app.model.UcoinTxIssuers;
import io.ucoin.app.sqlite.SQLiteTable;

final public class TxIssuers extends Table
        implements UcoinTxIssuers {

    private Long mTxId;

    public TxIssuers(Context context, Long txId) {
        this(context, txId,
                SQLiteTable.TxIssuer.TX_ID + "=?",
                new String[]{txId.toString()});
    }

    private TxIssuers(Context context, Long txId, String selection, String[] selectionArgs) {
        this(context, txId, selection, selectionArgs, null);
    }

    private TxIssuers(Context context, Long txId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.TX_ISSUER_URI, selection, selectionArgs, sortOrder);
        mTxId = txId;
    }

    @Override
    public UcoinTxIssuer add(String publicKey, Integer issuerOrder) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.TxIssuer.TX_ID, mTxId);
        values.put(SQLiteTable.TxIssuer.PUBLIC_KEY, publicKey);
        values.put(SQLiteTable.TxIssuer.ISSUER_ORDER, issuerOrder);

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new TxIssuer(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxIssuer getById(Long id) {
        return new TxIssuer(mContext, id);
    }

    @Override
    public Iterator<UcoinTxIssuer> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinTxIssuer> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new TxIssuer(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}