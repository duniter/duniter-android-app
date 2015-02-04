package io.ucoin.app.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import io.ucoin.app.R;
import io.ucoin.app.database.Contract;
import io.ucoin.app.database.DbHelper;

/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class Provider extends ContentProvider implements Contract {

    private DbHelper mDbHelper;
    private static final int ACCOUNT = 10;
    private static final int ACCOUNT_ID = 11;
    private static final int COMMUNITY = 20;
    private static final int PEER = 30;





    public static Uri CONTENT_URI;
    static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = new DbHelper(getContext(), context.getString(R.string.DBNAME),
                null, context.getResources().getInteger(R.integer.DBVERSION));

        String AUTHORITY = getContext().getString(R.string.AUTHORITY);
        CONTENT_URI = Uri.parse("content://" + AUTHORITY);
        uriMatcher.addURI(AUTHORITY, "account/", ACCOUNT);
        uriMatcher.addURI(AUTHORITY, "account/#", ACCOUNT_ID);
        uriMatcher.addURI(AUTHORITY, "community/", COMMUNITY);
        uriMatcher.addURI(AUTHORITY, "peer/", PEER);

        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int uriInt = uriMatcher.match(uri);
        if(uriInt == -1) {
            Log.d("PROVIDER", "NO MATCH URI");
            return null;
        }

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor = null;

        switch(uriInt) {
            case ACCOUNT :
                queryBuilder.setTables(Account.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case ACCOUNT_ID :
                queryBuilder.setTables(Account.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, Account._ID + "=1",
                        null, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case COMMUNITY :
                queryBuilder.setTables(Community.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        long id;
        switch (uriType) {
            case ACCOUNT:
                id = db.insert(Account.TABLE_NAME, null, values);
                uri = Uri.parse("identity/" + id);
                break;
            case COMMUNITY:
                id = db.insert(Community.TABLE_NAME, null, values);
                uri = Uri.parse("community/" + id);
                break;
            case PEER:
                id = db.insert(Peer.TABLE_NAME, null, values);
                uri = Uri.parse("peer/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
    /*
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        return 0;
    }
}