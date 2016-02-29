package io.ucoin.app.model.sql.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import io.ucoin.app.model.SqlRow;

public class Row implements SqlRow {

    protected Long mId;
    protected Context mContext;
    protected Uri mUri;

    public Row(Context context) {
        this(context, null, null);
    }

    public Row(Context context, Uri uri, Long id) {
        mContext = context;
        mUri = uri;
        mId = id;
    }

    public Long id() {
        return mId;
    }

    @Override
    public int delete() {
        Uri uri = Uri.withAppendedPath(mUri, mId.toString());
        return mContext.getContentResolver().delete(uri, null, null);
    }

    @Override
    public int update(ContentValues values) {
        Uri uri = Uri.withAppendedPath(mUri, mId.toString());
        return mContext.getContentResolver().update(uri, values, null, null);
    }

    public Cursor fetch() {
        return mContext.getContentResolver().query(
                mUri,
                null,
                BaseColumns._ID,
                new String[]{mId.toString()},
                null);
    }

    /**
     * * getters ***
     */
    public String getString(String field) {
        Cursor cursor = getField(field);
        if (cursor == null) {
            return null;
        }
        String result = cursor.getString(cursor.getColumnIndex(field));
        cursor.close();
        return result;
    }


    public Long getLong(String field) {
        Cursor cursor = getField(field);
        if (cursor == null) {
            return null;
        }

        int fieldIndex = cursor.getColumnIndex(field);
        Long result = (cursor.isNull(cursor.getColumnIndex(field))) ? null : cursor.getLong(fieldIndex);
        cursor.close();
        return result;
    }

    public Integer getInt(String field) {
        Cursor cursor = getField(field);
        if (cursor == null) {
            return null;
        }
        int fieldIndex = cursor.getColumnIndex(field);
        Integer result = (cursor.isNull(cursor.getColumnIndex(field))) ? null : cursor.getInt(fieldIndex);
        cursor.close();
        return result;
    }

    public Float getFloat(String field) {
        Cursor cursor = getField(field);
        if (cursor == null) {
            return null;
        }

        int fieldIndex = cursor.getColumnIndex(field);
        Float result = (cursor.isNull(cursor.getColumnIndex(field))) ? null : cursor.getFloat(fieldIndex);
        cursor.close();
        return result;
    }

    public Double getDouble(String field) {
        Cursor cursor = getField(field);
        if (cursor == null) {
            return null;
        }

        int fieldIndex = cursor.getColumnIndex(field);
        Double result = (cursor.isNull(cursor.getColumnIndex(field))) ? null : cursor.getDouble(fieldIndex);
        cursor.close();
        return result;
    }

    public Boolean getBoolean(String field) {
        Cursor cursor = getField(field);
        if (cursor == null) {
            return null;
        }
        String result = cursor.getString(cursor.getColumnIndex(field));
        cursor.close();
        return Boolean.valueOf(result);
    }

    /**
     * * INTERNAL methods ***
     */
    private Cursor getField(String field) {
        Uri uri = Uri.withAppendedPath(mUri, mId.toString());
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{field},
                null, null, null);

        if (!cursor.moveToNext())
            return null;

        return cursor;
    }
}