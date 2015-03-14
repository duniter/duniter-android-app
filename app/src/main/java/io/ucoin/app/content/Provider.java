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
import io.ucoin.app.database.DatabaseHelper;

/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class Provider extends ContentProvider implements Contract {

    private DatabaseHelper mDatabaseHelper;
    private static final int ACCOUNT = 10;
    private static final int ACCOUNT_ID = 11;
    private static final int CURRENCY = 20;
    private static final int PEER = 30;
    private static final int WALLET = 40;
    private static final int MOVEMENT = 50;
    private static final int CONTACT = 60;
    private static final int CONTACT2CURRENCY = 70;
    private static final int CONTACT_VIEW = 80;


    public static Uri CONTENT_URI;
    static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDatabaseHelper = new DatabaseHelper(getContext(), context.getString(R.string.DBNAME),
                null, context.getResources().getInteger(R.integer.DBVERSION));

        String AUTHORITY = getContext().getString(R.string.AUTHORITY);
        CONTENT_URI = Uri.parse("content://" + AUTHORITY);
        uriMatcher.addURI(AUTHORITY, "account/", ACCOUNT);
        uriMatcher.addURI(AUTHORITY, "account/#", ACCOUNT_ID);
        uriMatcher.addURI(AUTHORITY, "currency/", CURRENCY);
        uriMatcher.addURI(AUTHORITY, "wallet/", WALLET);
        uriMatcher.addURI(AUTHORITY, "peer/", PEER);
        uriMatcher.addURI(AUTHORITY, "movement/", MOVEMENT);
        uriMatcher.addURI(AUTHORITY, "contact/", CONTACT);
        uriMatcher.addURI(AUTHORITY, "contact2currency/", CONTACT2CURRENCY);
        uriMatcher.addURI(AUTHORITY, "contactView/", CONTACT_VIEW);

        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
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
            case CURRENCY :
                queryBuilder.setTables(Currency.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case PEER :
                queryBuilder.setTables(Peer.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case WALLET :
                queryBuilder.setTables(Wallet.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case MOVEMENT :
                queryBuilder.setTables(Movement.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case CONTACT :
                queryBuilder.setTables(Contact.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case CONTACT2CURRENCY :
                queryBuilder.setTables(Contact2Currency.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case CONTACT_VIEW :
                queryBuilder.setTables(ContactView.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        long id;
        switch (uriType) {
            case ACCOUNT:
                id = db.insert(Account.TABLE_NAME, null, values);
                uri = Uri.parse("identity/" + id);
                break;
            case CURRENCY:
                id = db.insert(Currency.TABLE_NAME, null, values);
                uri = Uri.parse("currency/" + id);
                break;
            case PEER:
                id = db.insert(Peer.TABLE_NAME, null, values);
                uri = Uri.parse("peer/" + id);
                break;
            case WALLET:
                id = db.insert(Wallet.TABLE_NAME, null, values);
                uri = Uri.parse("wallet/" + id);
                break;
            case MOVEMENT:
                id = db.insert(Movement.TABLE_NAME, null, values);
                uri = Uri.parse("movement/" + id);
                break;
            case CONTACT:
                id = db.insert(Contact.TABLE_NAME, null, values);
                uri = Uri.parse("contact/" + id);
                break;
            case CONTACT2CURRENCY:
                id = db.insert(Contact2Currency.TABLE_NAME, null, values);
                uri = Uri.parse("contact2currency/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }


    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        int nbRowsUpdated;
        switch (uriType) {
            case ACCOUNT:
                nbRowsUpdated = db.delete(Account.TABLE_NAME, whereClause, whereArgs);
                break;
            case CURRENCY:
                nbRowsUpdated = db.delete(Currency.TABLE_NAME, whereClause, whereArgs);
                break;
            case PEER:
                nbRowsUpdated = db.delete(Peer.TABLE_NAME, whereClause, whereArgs);
                break;
            case WALLET:
                nbRowsUpdated = db.delete(Wallet.TABLE_NAME, whereClause, whereArgs);
                break;
            case MOVEMENT:
                nbRowsUpdated = db.delete(Movement.TABLE_NAME, whereClause, whereArgs);
                break;
            case CONTACT:
                nbRowsUpdated = db.delete(Contact.TABLE_NAME, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return nbRowsUpdated;
    }

    /*
     * update rows
     */
    public int update(
            Uri uri,
            ContentValues values,
            String whereClause,
            String[] whereArgs) {

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        int nbRowsUpdated;
        switch (uriType) {
            case ACCOUNT:
                nbRowsUpdated = db.update(Account.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case CURRENCY:
                nbRowsUpdated = db.update(Currency.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case PEER:
                nbRowsUpdated = db.update(Peer.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case WALLET:
                nbRowsUpdated = db.update(Wallet.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case MOVEMENT:
                nbRowsUpdated = db.update(Movement.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case CONTACT:
                nbRowsUpdated = db.update(Contact.TABLE_NAME, values, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return nbRowsUpdated;
    }

}