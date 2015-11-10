package io.ucoin.app.content;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.dao.sqlite.SQLiteHelper;
import io.ucoin.app.dao.sqlite.SQLiteTable;

/*
 * Define an implementation of ContentProvider that stubs out
 * all methods
 */
public class Provider extends ContentProvider implements SQLiteTable {

    static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int ACCOUNT = 10;
    private static final int ACCOUNT_ID = 11;
    private static final int CURRENCY = 20;
    private static final int CURRENCY_ID = 21;
    private static final int BLOCKCHAIN_PARAMETERS = 100;
    private static final int BLOCKCHAIN_PARAMETERS_ID = 101;
    private static final int PEER = 30;
    private static final int PEER_ID = 31;
    private static final int WALLET = 40;
    private static final int WALLET_ID = 41;
    private static final int MOVEMENT = 50;
    private static final int CONTACT = 60;
    private static final int CONTACT_ID = 61;
    private static final int CONTACT2CURRENCY = 70;
    private static final int CONTACT_VIEW = 80;
    private static final int UD = 90;

    /**
     * @deprecated use specific URI instead
     */
    @Deprecated
    public static Uri CONTENT_URI;

    public static Uri ACCOUNT_URI;
    public static Uri CURRENCY_URI;
    public static Uri BLOCKCHAIN_PARAMETERS_URI;
    public static Uri PEER_URI;
    public static Uri WALLET_URI;
    public static Uri MOVEMENT_URI;
    public static Uri CONTACT_URI;
    public static Uri CONTACT2CURRENCY_URI;
    public static Uri CONTACT_VIEW_URI;
    public static Uri UD_URI;

    private SQLiteHelper mSQLiteHelper;

    public static void initUris(Context context) {

        String AUTHORITY = context.getString(R.string.AUTHORITY);

        CONTENT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY).build();

        ACCOUNT_URI  = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.account_uri)).build();
        CURRENCY_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.currency_uri)).build();
        BLOCKCHAIN_PARAMETERS_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.blockchain_parameters_uri)).build();
        WALLET_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.wallet_uri)).build();
        PEER_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.peer_uri)).build();
        CONTACT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.contact_uri)).build();
        UD_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.ud_uri)).build();
        MOVEMENT_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.movement_uri)).build();
        CONTACT2CURRENCY_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.contact2currency_uri)).build();
        CONTACT_VIEW_URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
                .path(context.getString(R.string.contact_view_uri)).build();

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.currency_uri), CURRENCY);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.currency_uri) + "#", CURRENCY_ID);

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.blockchain_parameters_uri), BLOCKCHAIN_PARAMETERS);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.blockchain_parameters_uri) + "#", BLOCKCHAIN_PARAMETERS_ID);

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.account_uri), ACCOUNT);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.account_uri) + "#", ACCOUNT_ID);

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.wallet_uri), WALLET);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.wallet_uri) + "#", WALLET_ID);

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.peer_uri), PEER);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.peer_uri) + "#", PEER_ID);

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.contact_uri), CONTACT);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.contact_uri) + "#", CONTACT_ID);

        uriMatcher.addURI(AUTHORITY, context.getString(R.string.ud_uri), UD);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.movement_uri), MOVEMENT);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.contact2currency_uri), CONTACT2CURRENCY);
        uriMatcher.addURI(AUTHORITY, context.getString(R.string.contact_view_uri), CONTACT_VIEW);
    }

    @Override
    public boolean onCreate() {
        Log.d("PROVIDER", "onCreate()");
        Context context = getContext();
        initUris(context);
        mSQLiteHelper = new SQLiteHelper(getContext(), context.getString(R.string.DBNAME),
                null, context.getResources().getInteger(R.integer.DBVERSION));
        return true;
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        SQLiteDatabase db = mSQLiteHelper.getReadableDatabase();
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
            case CURRENCY_ID:
                queryBuilder.setTables(Currency.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case BLOCKCHAIN_PARAMETERS:
                queryBuilder.setTables(BlockchainParameters.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case BLOCKCHAIN_PARAMETERS_ID:
                queryBuilder.setTables(BlockchainParameters.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case UD :
                queryBuilder.setTables(SQLiteTable.UD.TABLE_NAME);
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
            case PEER_ID:
                queryBuilder.setTables(Peer.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case WALLET :
                queryBuilder.setTables(Wallet.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case WALLET_ID:
                queryBuilder.setTables(Wallet.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
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
            case CONTACT_ID:
                queryBuilder.setTables(Contact.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
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
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
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
            case BLOCKCHAIN_PARAMETERS:
                id = db.insert(BlockchainParameters.TABLE_NAME, null, values);
                uri = Uri.parse("blockchain/parameters/");
                break;
            case UD:
                id = db.insert(SQLiteTable.UD.TABLE_NAME, null, values);
                uri = Uri.parse("ud/" + id);
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
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        int nbRowsUpdated;
        switch (uriType) {
            case ACCOUNT:
                nbRowsUpdated = db.delete(Account.TABLE_NAME, whereClause, whereArgs);
                break;
            case CURRENCY:
                nbRowsUpdated = db.delete(Currency.TABLE_NAME, whereClause, whereArgs);
                break;
            case BLOCKCHAIN_PARAMETERS:
                nbRowsUpdated = db.delete(BlockchainParameters.TABLE_NAME, whereClause, whereArgs);
                break;
            case UD:
                nbRowsUpdated = db.delete(SQLiteTable.UD.TABLE_NAME, whereClause, whereArgs);
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
            case CONTACT2CURRENCY:
                nbRowsUpdated = db.delete(Contact2Currency.TABLE_NAME, whereClause, whereArgs);
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

        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        int nbRowsUpdated;
        switch (uriType) {
            case ACCOUNT:
                nbRowsUpdated = db.update(Account.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case CURRENCY:
                nbRowsUpdated = db.update(Currency.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case BLOCKCHAIN_PARAMETERS:
                nbRowsUpdated = db.update(BlockchainParameters.TABLE_NAME, values, whereClause, whereArgs);
                break;
            case UD:
                nbRowsUpdated = db.update(SQLiteTable.UD.TABLE_NAME, values, whereClause, whereArgs);
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
            case CONTACT2CURRENCY:
                nbRowsUpdated = db.update(Contact2Currency.TABLE_NAME, values, whereClause, whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return nbRowsUpdated;
    }

    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {

        return super.applyBatch(operations);
    }
}