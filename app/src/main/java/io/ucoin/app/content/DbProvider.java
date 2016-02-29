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

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.sqlite.SQLiteHelper;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;

public class DbProvider extends ContentProvider implements SQLiteTable {
    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int CURRENCY = 10;
    private static final int CURRENCY_ID = 11;
    private static final int IDENTITY = 20;
    private static final int IDENTITY_ID = 21;
    private static final int PEER = 30;
    private static final int PEER_ID = 31;
    private static final int ENDPOINT = 40;
    private static final int ENDPOINT_ID = 41;
    private static final int WALLET = 50;
    private static final int WALLET_ID = 51;
    private static final int SOURCE = 60;
    private static final int SOURCE_ID = 61;
    private static final int TX = 70;
    private static final int TX_ID = 71;
    private static final int TX_ISSUER = 72;
    private static final int TX_ISSUER_ID = 73;
    private static final int TX_INPUT = 74;
    private static final int TX_INPUT_ID = 75;
    private static final int TX_OUTPUT = 76;
    private static final int TX_OUTPUT_ID = 77;
    private static final int TX_SIGNATURE = 78;
    private static final int TX_SIGNATURE_ID = 79;
    private static final int MEMBER = 90;
    private static final int MEMBER_ID = 91;
    private static final int CERTIFICATION = 100;
    private static final int CERTIFICATION_ID = 101;
    private static final int BLOCK = 110;
    private static final int BLOCK_ID = 111;
    private static final int UD = 120;
    private static final int UD_ID = 121;
    private static final int MEMBERSHIP = 130;
    private static final int MEMBERSHIP_ID = 131;
    private static final int SELF_CERTIFICATION = 140;
    private static final int SELF_CERTIFICATION_ID = 141;
    private static final int CONTACT = 150;
    private static final int CONTACT_ID = 151;

    private static final int OPERATION = 200;
    private static final int OPERATION_ID = 201;

    private static final int REQUETE = 300;

    private SQLiteHelper mSQLiteHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();

        mSQLiteHelper = new SQLiteHelper(context, context.getString(R.string.DBNAME),
                null, context.getResources().getInteger(R.integer.DBVERSION));
        uriMatcher.addURI(UcoinUris.CURRENCY_URI.getAuthority(), UcoinUris.CURRENCY_URI.getPath(), CURRENCY);
        uriMatcher.addURI(UcoinUris.CURRENCY_URI.getAuthority(), UcoinUris.CURRENCY_URI.getPath() + "#", CURRENCY_ID);

        uriMatcher.addURI(UcoinUris.IDENTITY_URI.getAuthority(), UcoinUris.IDENTITY_URI.getPath(), IDENTITY);
        uriMatcher.addURI(UcoinUris.IDENTITY_URI.getAuthority(), UcoinUris.IDENTITY_URI.getPath() + "#", IDENTITY_ID);

        uriMatcher.addURI(UcoinUris.PEER_URI.getAuthority(), UcoinUris.PEER_URI.getPath(), PEER);
        uriMatcher.addURI(UcoinUris.PEER_URI.getAuthority(), UcoinUris.PEER_URI.getPath() + "#", PEER_ID);

        uriMatcher.addURI(UcoinUris.ENDPOINT_URI.getAuthority(), UcoinUris.ENDPOINT_URI.getPath(), ENDPOINT);
        uriMatcher.addURI(UcoinUris.ENDPOINT_URI.getAuthority(), UcoinUris.ENDPOINT_URI.getPath() + "#", ENDPOINT_ID);

        uriMatcher.addURI(UcoinUris.WALLET_URI.getAuthority(), UcoinUris.WALLET_URI.getPath(), WALLET);
        uriMatcher.addURI(UcoinUris.WALLET_URI.getAuthority(), UcoinUris.WALLET_URI.getPath() + "#", WALLET_ID);

        uriMatcher.addURI(UcoinUris.SOURCE_URI.getAuthority(), UcoinUris.SOURCE_URI.getPath(), SOURCE);
        uriMatcher.addURI(UcoinUris.SOURCE_URI.getAuthority(), UcoinUris.SOURCE_URI.getPath() + "#", SOURCE_ID);

        uriMatcher.addURI(UcoinUris.TX_URI.getAuthority(), UcoinUris.TX_URI.getPath(), TX);
        uriMatcher.addURI(UcoinUris.TX_URI.getAuthority(), UcoinUris.TX_URI.getPath() + "#", TX_ID);

        uriMatcher.addURI(UcoinUris.TX_ISSUER_URI.getAuthority(), UcoinUris.TX_ISSUER_URI.getPath(), TX_ISSUER);
        uriMatcher.addURI(UcoinUris.TX_ISSUER_URI.getAuthority(), UcoinUris.TX_ISSUER_URI.getPath() + "#", TX_ISSUER_ID);
        uriMatcher.addURI(UcoinUris.TX_INPUT_URI.getAuthority(), UcoinUris.TX_INPUT_URI.getPath(), TX_INPUT);
        uriMatcher.addURI(UcoinUris.TX_INPUT_URI.getAuthority(), UcoinUris.TX_INPUT_URI.getPath() + "#", TX_INPUT_ID);
        uriMatcher.addURI(UcoinUris.TX_OUTPUT_URI.getAuthority(), UcoinUris.TX_OUTPUT_URI.getPath(), TX_OUTPUT);
        uriMatcher.addURI(UcoinUris.TX_OUTPUT_URI.getAuthority(), UcoinUris.TX_OUTPUT_URI.getPath() + "#", TX_OUTPUT_ID);
        uriMatcher.addURI(UcoinUris.TX_SIGNATURE_URI.getAuthority(), UcoinUris.TX_SIGNATURE_URI.getPath(), TX_SIGNATURE);
        uriMatcher.addURI(UcoinUris.TX_SIGNATURE_URI.getAuthority(), UcoinUris.TX_SIGNATURE_URI.getPath() + "#", TX_SIGNATURE_ID);

        uriMatcher.addURI(UcoinUris.MEMBER_URI.getAuthority(), UcoinUris.MEMBER_URI.getPath(), MEMBER);
        uriMatcher.addURI(UcoinUris.MEMBER_URI.getAuthority(), UcoinUris.MEMBER_URI.getPath() + "#", MEMBER_ID);

        uriMatcher.addURI(UcoinUris.CERTIFICATION_URI.getAuthority(), UcoinUris.CERTIFICATION_URI.getPath(), CERTIFICATION);
        uriMatcher.addURI(UcoinUris.CERTIFICATION_URI.getAuthority(), UcoinUris.CERTIFICATION_URI.getPath() + "#", CERTIFICATION_ID);

        uriMatcher.addURI(UcoinUris.BLOCK_URI.getAuthority(), UcoinUris.BLOCK_URI.getPath(), BLOCK);
        uriMatcher.addURI(UcoinUris.BLOCK_URI.getAuthority(), UcoinUris.BLOCK_URI.getPath() + "#", BLOCK_ID);

        uriMatcher.addURI(UcoinUris.UD_URI.getAuthority(), UcoinUris.UD_URI.getPath(), UD);
        uriMatcher.addURI(UcoinUris.UD_URI.getAuthority(), UcoinUris.UD_URI.getPath() + "#", UD_ID);

        uriMatcher.addURI(UcoinUris.MEMBERSHIP_URI.getAuthority(), UcoinUris.MEMBERSHIP_URI.getPath(), MEMBERSHIP);
        uriMatcher.addURI(UcoinUris.MEMBERSHIP_URI.getAuthority(), UcoinUris.MEMBERSHIP_URI.getPath() + "#", MEMBERSHIP_ID);

        uriMatcher.addURI(UcoinUris.SELF_CERTIFICATION_URI.getAuthority(), UcoinUris.SELF_CERTIFICATION_URI.getPath(), SELF_CERTIFICATION);
        uriMatcher.addURI(UcoinUris.SELF_CERTIFICATION_URI.getAuthority(), UcoinUris.SELF_CERTIFICATION_URI.getPath() + "#", SELF_CERTIFICATION_ID);

        uriMatcher.addURI(UcoinUris.CONTACT_URI.getAuthority(), UcoinUris.CONTACT_URI.getPath(), CONTACT);
        uriMatcher.addURI(UcoinUris.CONTACT_URI.getAuthority(), UcoinUris.CONTACT_URI.getPath() + "#", CONTACT_ID);

        uriMatcher.addURI(UcoinUris.OPERATION_URI.getAuthority(), UcoinUris.OPERATION_URI.getPath(), OPERATION);
        uriMatcher.addURI(UcoinUris.OPERATION_URI.getAuthority(), UcoinUris.OPERATION_URI.getPath() + "#", OPERATION_ID);

        uriMatcher.addURI(UcoinUris.REQUETE_URI.getAuthority(), UcoinUris.REQUETE_URI.getPath(), REQUETE);

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

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        Cursor cursor;

        switch (uriInt) {
            case CURRENCY:
                queryBuilder.setTables(SQLiteView.Currency.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CURRENCY_ID:
                queryBuilder.setTables(SQLiteView.Currency.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case IDENTITY:
                queryBuilder.setTables(SQLiteView.Identity.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case IDENTITY_ID:
                queryBuilder.setTables(SQLiteView.Identity.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case PEER:
                queryBuilder.setTables(Peer.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PEER_ID:
                queryBuilder.setTables(Peer.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case ENDPOINT:
                queryBuilder.setTables(Endpoint.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ENDPOINT_ID:
                queryBuilder.setTables(Endpoint.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case WALLET:
                queryBuilder.setTables(SQLiteView.Wallet.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case WALLET_ID:
                queryBuilder.setTables(SQLiteView.Wallet.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        Wallet._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case SOURCE:
                queryBuilder.setTables(Source.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case SOURCE_ID:
                queryBuilder.setTables(Source.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case MEMBER:
                queryBuilder.setTables(SQLiteView.Member.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case MEMBER_ID:
                queryBuilder.setTables(SQLiteView.Member.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case CERTIFICATION:
                queryBuilder.setTables(SQLiteView.Certification.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CERTIFICATION_ID:
                queryBuilder.setTables(SQLiteView.Certification.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case BLOCK:
                queryBuilder.setTables(Block.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case BLOCK_ID:
                queryBuilder.setTables(Block.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case TX:
                queryBuilder.setTables(SQLiteView.Tx.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case TX_ID:
                queryBuilder.setTables(SQLiteView.Tx.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case TX_ISSUER:
                queryBuilder.setTables(TxIssuer.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TX_ISSUER_ID:
                queryBuilder.setTables(TxIssuer.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case TX_INPUT:
                queryBuilder.setTables(TxInput.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case TX_INPUT_ID:
                queryBuilder.setTables(TxInput.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case TX_OUTPUT:
                queryBuilder.setTables(TxOutput.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case TX_OUTPUT_ID:
                queryBuilder.setTables(TxOutput.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case TX_SIGNATURE:
                queryBuilder.setTables(TxSignature.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case TX_SIGNATURE_ID:
                queryBuilder.setTables(TxSignature.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case UD:
                queryBuilder.setTables(SQLiteView.Ud.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case UD_ID:
                queryBuilder.setTables(SQLiteView.Ud.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case MEMBERSHIP:
                queryBuilder.setTables(SQLiteView.Membership.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case MEMBERSHIP_ID:
                queryBuilder.setTables(SQLiteView.Membership.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case SELF_CERTIFICATION:
                queryBuilder.setTables(SelfCertification.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case SELF_CERTIFICATION_ID:
                queryBuilder.setTables(SelfCertification.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case CONTACT:
                queryBuilder.setTables(Contact.TABLE_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CONTACT_ID:
                queryBuilder.setTables(Contact.TABLE_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case OPERATION:
                queryBuilder.setTables(SQLiteView.Operation.VIEW_NAME);
                cursor = queryBuilder.query(db, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case OPERATION_ID:
                queryBuilder.setTables(SQLiteView.Operation.VIEW_NAME);
                cursor = queryBuilder.query(db, null,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()},
                        null, null, null);
                break;
            case REQUETE:
                cursor = db.rawQuery(selection, selectionArgs);
                break;

            default:
                throw new RuntimeException("NO MATCH URI : " + uri.toString());
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
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
            case CURRENCY:
                id = db.insertWithOnConflict(Currency.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.CURRENCY_URI + Long.toString(id));
                break;
            case IDENTITY:
                id = db.insertWithOnConflict(Identity.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.IDENTITY_URI + Long.toString(id));
                break;
            case PEER:
                id = db.insertWithOnConflict(Peer.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                uri = Uri.parse(UcoinUris.PEER_URI + Long.toString(id));
                break;
            case ENDPOINT:
                id = db.insertWithOnConflict(Endpoint.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                uri = Uri.parse(UcoinUris.ENDPOINT_URI + Long.toString(id));
                break;
            case WALLET:
                id = db.insertWithOnConflict(Wallet.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.WALLET_URI + Long.toString(id));
                break;
            case SOURCE:
                id = db.insertWithOnConflict(Source.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.SOURCE_URI + Long.toString(id));
                break;
            case MEMBER:
                id = db.insertWithOnConflict(Member.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.MEMBER_URI + Long.toString(id));
                break;
            case CERTIFICATION:
                id = db.insertWithOnConflict(Certification.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.CERTIFICATION_URI + Long.toString(id));
                break;
            case BLOCK:
                id = db.insertWithOnConflict(Block.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.BLOCK_URI + Long.toString(id));
                break;
            case TX:
                id = db.insertWithOnConflict(Tx.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.TX_URI + Long.toString(id));
                break;
            case TX_ISSUER:
                id = db.insertWithOnConflict(TxIssuer.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                uri = Uri.parse(UcoinUris.TX_ISSUER_URI + Long.toString(id));
                break;
            case TX_INPUT:
                id = db.insertWithOnConflict(TxInput.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                uri = Uri.parse(UcoinUris.TX_INPUT_URI + Long.toString(id));
                break;
            case TX_OUTPUT:
                id = db.insertWithOnConflict(TxOutput.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                uri = Uri.parse(UcoinUris.TX_OUTPUT_URI + Long.toString(id));
                break;
            case TX_SIGNATURE:
                id = db.insertWithOnConflict(TxSignature.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_FAIL);
                uri = Uri.parse(UcoinUris.TX_SIGNATURE_URI + Long.toString(id));
                break;
            case UD:
                id = db.insertWithOnConflict(Ud.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.UD_URI + Long.toString(id));
                break;
            case MEMBERSHIP:
                id = db.insertWithOnConflict(Membership.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.MEMBERSHIP_URI + Long.toString(id));
                break;
            case SELF_CERTIFICATION:
                id = db.insertWithOnConflict(SelfCertification.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.SELF_CERTIFICATION_URI + Long.toString(id));
                break;
            case CONTACT:
                id = db.insertWithOnConflict(Contact.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                uri = Uri.parse(UcoinUris.CONTACT_URI + Long.toString(id));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        notifyChange(uriType);
        return uri;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();

        int uriType = uriMatcher.match(uri);
        int deletedRows = 0;

        switch (uriType) {
            case CURRENCY:
                deletedRows = db.delete(Currency.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CURRENCY_ID:
                deletedRows = db.delete(Currency.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case BLOCK:
                deletedRows = db.delete(Block.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case BLOCK_ID:
                deletedRows = db.delete(Block.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;

            case IDENTITY:
                deletedRows = db.delete(Identity.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case IDENTITY_ID:
                deletedRows = db.delete(Identity.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case PEER:
                deletedRows = db.delete(Peer.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case PEER_ID:
                deletedRows = db.delete(Peer.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case ENDPOINT:
                deletedRows = db.delete(Endpoint.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case ENDPOINT_ID:
                deletedRows = db.delete(Endpoint.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case WALLET:
                deletedRows = db.delete(Wallet.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case WALLET_ID:
                deletedRows = db.delete(Wallet.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case SOURCE:
                deletedRows = db.delete(Source.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case SOURCE_ID:
                deletedRows = db.delete(Source.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case MEMBER:
                deletedRows = db.delete(Member.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case MEMBER_ID:
                deletedRows = db.delete(Member.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case CERTIFICATION:
                deletedRows = db.delete(Certification.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CERTIFICATION_ID:
                deletedRows = db.delete(Certification.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TX:
                deletedRows = db.delete(Tx.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TX_ID:
                deletedRows = db.delete(Tx.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TX_ISSUER:
                deletedRows = db.delete(TxIssuer.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TX_ISSUER_ID:
                deletedRows = db.delete(TxIssuer.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TX_INPUT:
                deletedRows = db.delete(TxInput.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TX_INPUT_ID:
                deletedRows = db.delete(TxInput.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TX_OUTPUT:
                deletedRows = db.delete(TxOutput.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TX_OUTPUT_ID:
                deletedRows = db.delete(TxOutput.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TX_SIGNATURE:
                deletedRows = db.delete(TxSignature.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TX_SIGNATURE_ID:
                deletedRows = db.delete(TxSignature.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case SELF_CERTIFICATION_ID:
                deletedRows = db.delete(SelfCertification.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case CONTACT_ID:
                deletedRows = db.delete(Contact.TABLE_NAME,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;

        }

        notifyChange(uriType);
        return deletedRows;
    }

    @Override
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();

        int uriType = uriMatcher.match(uri);
        int updatedRows;

        switch (uriType) {
            case CURRENCY_ID:
                updatedRows = db.update(Currency.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case BLOCK_ID:
                updatedRows = db.update(Block.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case WALLET_ID:
                updatedRows = db.update(Wallet.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case IDENTITY_ID:
                updatedRows = db.update(Identity.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case SOURCE_ID:
                updatedRows = db.update(Source.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case MEMBER_ID:
                updatedRows = db.update(Member.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case ENDPOINT_ID:
                updatedRows = db.update(Endpoint.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TX_ID:
                updatedRows = db.update(Tx.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case UD_ID:
                updatedRows = db.update(Ud.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case MEMBERSHIP_ID:
                updatedRows = db.update(Membership.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case SELF_CERTIFICATION_ID:
                updatedRows = db.update(SelfCertification.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case CONTACT_ID:
                updatedRows = db.update(Contact.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;

            case CERTIFICATION_ID:
                updatedRows = db.update(Certification.TABLE_NAME,
                        values,
                        BaseColumns._ID + "=?",
                        new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        notifyChange(uriType);
        return updatedRows;
    }

    private void notifyChange(int uriType) {
        switch (uriType) {
            case CURRENCY:
            case CURRENCY_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.CURRENCY_URI, null);
                break;
            case IDENTITY:
            case IDENTITY_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.IDENTITY_URI, null);
                notifyChange(CURRENCY);
                break;
            case PEER:
            case PEER_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.PEER_URI, null);
                notifyChange(CURRENCY);
                break;
            case ENDPOINT:
            case ENDPOINT_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.ENDPOINT_URI, null);
                notifyChange(PEER);
                break;
            case WALLET:
            case WALLET_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.WALLET_URI, null);
                notifyChange(CURRENCY);
                break;
            case SOURCE:
            case SOURCE_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.SOURCE_URI, null);
                notifyChange(WALLET);
                break;
            case TX:
            case TX_ID:
            case TX_ISSUER:
            case TX_ISSUER_ID:
            case TX_INPUT:
            case TX_INPUT_ID:
            case TX_OUTPUT:
            case TX_OUTPUT_ID:
            case TX_SIGNATURE:
            case TX_SIGNATURE_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.TX_URI, null);
                getContext().getContentResolver().notifyChange(UcoinUris.OPERATION_URI, null);
                notifyChange(WALLET);
                break;
            case MEMBER:
            case MEMBER_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.MEMBER_URI, null);
                notifyChange(CERTIFICATION);
                notifyChange(CURRENCY);
                break;
            case CERTIFICATION:
            case CERTIFICATION_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.CERTIFICATION_URI, null);
                getContext().getContentResolver().notifyChange(UcoinUris.IDENTITY_URI, null);
                break;
            case BLOCK:
            case BLOCK_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.UD_URI, null);
                getContext().getContentResolver().notifyChange(UcoinUris.CURRENCY_URI, null);
                getContext().getContentResolver().notifyChange(UcoinUris.WALLET_URI, null);
                getContext().getContentResolver().notifyChange(UcoinUris.BLOCK_URI, null);
                notifyChange(CURRENCY);
                break;
            case UD:
            case UD_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.UD_URI, null);
                getContext().getContentResolver().notifyChange(UcoinUris.OPERATION_URI, null);
                notifyChange(WALLET);
                break;
            case MEMBERSHIP:
            case MEMBERSHIP_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.MEMBERSHIP_URI, null);
                break;
            case SELF_CERTIFICATION:
            case SELF_CERTIFICATION_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.SELF_CERTIFICATION_URI, null);
                break;
            case CONTACT:
            case CONTACT_ID:
                getContext().getContentResolver().notifyChange(UcoinUris.CONTACT_URI, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uriType);
        }
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
        SQLiteDatabase db = mSQLiteHelper.getWritableDatabase();
        ContentProviderResult result[];

        try {
            db.beginTransaction();
            result = super.applyBatch(operations);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            db.endTransaction();
            throw e;
        }

        db.endTransaction();
        return result;
    }

}