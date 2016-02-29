package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.model.UcoinTxOutput;
import io.ucoin.app.model.UcoinTxOutputs;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.sqlite.SQLiteTable;

final public class TxOutputs extends Table
        implements UcoinTxOutputs {

    private Long mTxId;

    public TxOutputs(Context context, Long txId) {
        this(context, txId,
                SQLiteTable.TxOutput.TX_ID + "=?",
                new String[]{txId.toString()});
    }

    public TxOutputs(Context context) {
        this(context, null, null,null);
    }

    private TxOutputs(Context context, Long txId, String selection, String[] selectionArgs) {
        this(context, txId, selection, selectionArgs, null);
    }

    private TxOutputs(Context context, Long txId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.TX_OUTPUT_URI, selection, selectionArgs, sortOrder);
        mTxId = txId;
    }

    @Override
    public UcoinTxOutput add(TxHistory.Tx.Output output) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.TxOutput.TX_ID, mTxId);
        values.put(SQLiteTable.TxOutput.PUBLIC_KEY, output.publicKey);
        values.put(SQLiteTable.TxOutput.AMOUNT, output.amount.toString());

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new TxOutput(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxOutput add(String publicKey, Long amount) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.TxOutput.TX_ID, mTxId);
        values.put(SQLiteTable.TxOutput.PUBLIC_KEY, publicKey);
        values.put(SQLiteTable.TxOutput.AMOUNT, amount);

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new TxOutput(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxOutput getById(Long id) {
        return new TxOutput(mContext, id);
    }

    @Override
    public UcoinTxOutputs getByOutput(String publicKey) {
        String selection = SQLiteTable.TxOutput.PUBLIC_KEY + "=?";
        String[] selectionArgs = new String[]{publicKey};
        return new TxOutputs(mContext, null, selection, selectionArgs);
    }

    @Override
    public Iterator<UcoinTxOutput> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinTxOutput> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new TxOutput(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}