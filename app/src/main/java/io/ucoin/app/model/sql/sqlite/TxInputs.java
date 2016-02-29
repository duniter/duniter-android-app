package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Iterator;

import io.ucoin.app.UcoinUris;
import io.ucoin.app.enumeration.SourceState;
import io.ucoin.app.model.UcoinSource;
import io.ucoin.app.model.UcoinTxInput;
import io.ucoin.app.model.UcoinTxInputs;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.sqlite.SQLiteTable;

final public class TxInputs extends Table
        implements UcoinTxInputs {

    private Long mTxId;

    public TxInputs(Context context, Long txId) {
        this(context, txId,
                SQLiteTable.TxInput.TX_ID + "=?",
                new String[]{txId.toString()},
                SQLiteTable.TxInput.ISSUER_INDEX + " ASC");
    }

    private TxInputs(Context context, Long txId, String selection, String[] selectionArgs) {
        this(context, txId, selection, selectionArgs, null);
    }

    private TxInputs(Context context, Long txId, String selection, String[] selectionArgs, String sortOrder) {
        super(context, UcoinUris.TX_INPUT_URI, selection, selectionArgs, sortOrder);
        mTxId = txId;
    }

    @Override
    public UcoinTxInput add(TxHistory.Tx.Input input) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.TxInput.TX_ID, mTxId);
        values.put(SQLiteTable.TxInput.ISSUER_INDEX, input.index);
        values.put(SQLiteTable.TxInput.TYPE, input.type.name());
        values.put(SQLiteTable.TxInput.NUMBER, input.number);
        values.put(SQLiteTable.TxInput.FINGERPRINT, input.fingerprint);
        values.put(SQLiteTable.TxInput.AMOUNT, input.amount.toString());

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new TxInput(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxInput add(UcoinSource source, Integer index) {
        ContentValues values = new ContentValues();
        values.put(SQLiteTable.TxInput.TX_ID, mTxId);
        values.put(SQLiteTable.TxInput.ISSUER_INDEX, index);
        values.put(SQLiteTable.TxInput.TYPE, source.type().name());
        values.put(SQLiteTable.TxInput.NUMBER, source.number());
        values.put(SQLiteTable.TxInput.FINGERPRINT, source.fingerprint());
        values.put(SQLiteTable.TxInput.AMOUNT, source.amount().toString());
        source.setState(SourceState.CONSUMED);

        Uri uri = insert(values);
        if (Long.parseLong(uri.getLastPathSegment()) > 0) {
            return new TxInput(mContext, Long.parseLong(uri.getLastPathSegment()));
        } else {
            return null;
        }
    }

    @Override
    public UcoinTxInput getById(Long id) {
        return new TxInput(mContext, id);
    }

    @Override
    public Iterator<UcoinTxInput> iterator() {
        Cursor cursor = fetch();
        if (cursor != null) {
            ArrayList<UcoinTxInput> data = new ArrayList<>();
            while (cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                data.add(new TxInput(mContext, id));
            }
            cursor.close();

            return data.iterator();
        }
        return null;
    }
}