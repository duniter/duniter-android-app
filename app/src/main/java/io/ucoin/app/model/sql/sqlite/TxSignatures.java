package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinTxSignature;
import io.ucoin.app.model.UcoinTxSignatures;
import io.ucoin.app.sqlite.SQLiteTable;

final public class TxSignatures extends Table
        implements UcoinTxSignatures {

    private Long mTxId;

    public TxSignatures(Context context, Long txId) {
        this(context, txId,
                SQLiteTable.TxSignature.TX_ID + "=?",
                new String[]{txId.toString()},
                SQLiteTable.TxSignature.ISSUER_ORDER + " ASC");
    }

    private TxSignatures(Context context, Long txId, String selection, String[] selectionArgs) {
        this(context, txId, selection, selectionArgs, null);
    }

    private TxSignatures(Context context, Long txId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.TX_SIGNATURE_URI, selection, selectionArgs, sortOrder);
        mTxId = txId;
    }

    @Override
    public UcoinTxSignature add(String signature, Integer issuerOrder) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.TxSignature.TX_ID, mTxId);
        values.put(SQLiteTable.TxSignature.VALUE, signature);
        values.put(SQLiteTable.TxSignature.ISSUER_ORDER, issuerOrder);

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new TxSignature(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxSignature getById(Long id) {
        return new TxSignature(mContext, id);
    }

    @Override
    public Iterator<UcoinTxSignature> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinTxSignature> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new TxSignature(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}