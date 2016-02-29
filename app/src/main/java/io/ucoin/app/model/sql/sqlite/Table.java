package io.ucoin.app.model.sql.sqlite;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.model.SqlTable;

public class Table implements SqlTable {

    protected Context mContext;
    protected Uri mUri;
    protected String mSelection;
    protected String[] mSelectionArgs;
    protected String mSortOrder;

    protected Table(Context context) {
        this(context, null, null, null, null);
    }

    protected Table(Context context, Uri uri) {
        this(context, uri, null, null, null);
    }

    protected Table(Context context, Uri uri, String selection, String[] selectionArgs) {
        this(context, uri, selection, selectionArgs, null);
    }

    protected Table(Context context, Uri uri, String selection, String[] selectionArgs, String sortOrder) {
        mContext = context;
        mUri = uri;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
    }

    @Override
    public Uri insert(ContentValues values) {
        return mContext.getContentResolver().insert(mUri, values);
    }


    public int delete(Long id) {
        return mContext.getContentResolver().delete(Uri.withAppendedPath(mUri, id.toString()), null, null);
    }

    @Override
    public Cursor query(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mContext.getContentResolver().query(mUri, projection, mSelection, mSelectionArgs, mSortOrder);
    }

    @Override
    public Integer count() {
        Cursor c = mContext.getContentResolver().query(
                mUri,
                null,
                mSelection,
                mSelectionArgs,
                mSortOrder);
        int count = c.getCount();
        c.close();
        return count;
    }

    @Override
    public int delete() {
        return mContext.getContentResolver().delete(mUri, mSelection, mSelectionArgs);
    }

    public Cursor fetch() {
        return mContext.getContentResolver().query(mUri, null,
                mSelection, mSelectionArgs, mSortOrder);
    }

    @Override
    public ContentProviderResult[] applyBatch (ArrayList<ContentProviderOperation> operations) throws RemoteException, OperationApplicationException {
        String authority = mContext.getResources().getString(R.string.AUTHORITY);
        return mContext.getContentResolver().applyBatch(authority, operations);
    }
}