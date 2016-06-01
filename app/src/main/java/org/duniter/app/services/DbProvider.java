package org.duniter.app.services;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.duniter.app.model.EntitySql.BlockUdSql;
import org.duniter.app.model.EntitySql.CertificationSql;
import org.duniter.app.model.EntitySql.ContactSql;
import org.duniter.app.model.EntitySql.CurrencySql;
import org.duniter.app.model.EntitySql.EndpointSql;
import org.duniter.app.model.EntitySql.IdentitySql;
import org.duniter.app.model.EntitySql.PeerSql;
import org.duniter.app.model.EntitySql.RequirementSql;
import org.duniter.app.model.EntitySql.SourceSql;
import org.duniter.app.model.EntitySql.TxSql;
import org.duniter.app.model.EntitySql.WalletSql;
import org.duniter.app.model.EntitySql.view.ViewCertificationAdapter;
import org.duniter.app.model.EntitySql.view.ViewTxAdapter;
import org.duniter.app.model.EntitySql.view.ViewWalletAdapter;
import org.duniter.app.model.EntitySql.view.ViewWalletIdentityAdapter;

/**
 * Created by naivalf27 on 26/04/16.
 */
public class DbProvider extends ContentProvider {
    private UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SqlHelper dbHelper;

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new SqlHelper(getContext());
        db = dbHelper.getWritableDatabase();
        initMatcher();
        return true;
    }

    private void initMatcher(){
        /*TABLE*/
        uriMatcher.addURI(CurrencySql.URI.getAuthority(),CurrencySql.URI.getPath(),CurrencySql.CODE);
        uriMatcher.addURI(BlockUdSql.URI.getAuthority(),BlockUdSql.URI.getPath(),BlockUdSql.CODE);
        uriMatcher.addURI(ContactSql.URI.getAuthority(),ContactSql.URI.getPath(),ContactSql.CODE);
        uriMatcher.addURI(EndpointSql.URI.getAuthority(),EndpointSql.URI.getPath(),EndpointSql.CODE);
        uriMatcher.addURI(IdentitySql.URI.getAuthority(),IdentitySql.URI.getPath(),IdentitySql.CODE);
        uriMatcher.addURI(PeerSql.URI.getAuthority(),PeerSql.URI.getPath(),PeerSql.CODE);
        uriMatcher.addURI(RequirementSql.URI.getAuthority(),RequirementSql.URI.getPath(),RequirementSql.CODE);
        uriMatcher.addURI(CertificationSql.URI.getAuthority(),CertificationSql.URI.getPath(),CertificationSql.CODE);
        uriMatcher.addURI(TxSql.URI.getAuthority(), TxSql.URI.getPath(),TxSql.CODE);
        uriMatcher.addURI(WalletSql.URI.getAuthority(),WalletSql.URI.getPath(),WalletSql.CODE);
        uriMatcher.addURI(SourceSql.URI.getAuthority(),SourceSql.URI.getPath(),SourceSql.CODE);

        /*VIEW*/
        uriMatcher.addURI(ViewWalletAdapter.URI.getAuthority(),ViewWalletAdapter.URI.getPath(),ViewWalletAdapter.CODE);
        uriMatcher.addURI(ViewWalletIdentityAdapter.URI.getAuthority(),ViewWalletIdentityAdapter.URI.getPath(),ViewWalletIdentityAdapter.CODE);
        uriMatcher.addURI(ViewCertificationAdapter.URI.getAuthority(),ViewCertificationAdapter.URI.getPath(),ViewCertificationAdapter.CODE);
        uriMatcher.addURI(ViewTxAdapter.URI.getAuthority(),ViewTxAdapter.URI.getPath(),ViewTxAdapter.CODE);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
//        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int uriType = uriMatcher.match(uri);
        Cursor cursor;
        switch (uriType){
            case CurrencySql.CODE:
                cursor = db.query(
                        CurrencySql.CurrencyTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BlockUdSql.CODE:
                cursor = db.query(
                        BlockUdSql.BlockTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ContactSql.CODE:
                cursor = db.query(
                        ContactSql.ContactTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case EndpointSql.CODE:
                cursor = db.query(
                        EndpointSql.EndpointTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case IdentitySql.CODE:
                cursor = db.query(
                        IdentitySql.IdentityTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PeerSql.CODE:
                cursor = db.query(
                        PeerSql.PeerTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case RequirementSql.CODE:
                cursor = db.query(
                        RequirementSql.RequirementTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CertificationSql.CODE:
                cursor = db.query(
                        CertificationSql.CertificationTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case TxSql.CODE:
                cursor = db.query(
                        TxSql.TxTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ViewTxAdapter.CODE:
                cursor = db.query(
                        ViewTxAdapter.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case WalletSql.CODE:
                cursor = db.query(
                        WalletSql.WalletTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ViewWalletAdapter.CODE:
                cursor = db.query(
                        ViewWalletAdapter.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ViewWalletIdentityAdapter.CODE:
                cursor = db.query(
                        ViewWalletIdentityAdapter.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ViewCertificationAdapter.CODE:
                cursor = db.query(
                        ViewCertificationAdapter.VIEW_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case SourceSql.CODE:
                cursor = db.query(
                        SourceSql.SourceTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                cursor = null;
                break;
        }
        if (cursor == null){
            throw new RuntimeException(String.format(
                    "Unknown URI: [%s]", uri));
        }else{
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            Log.d("DB QUERY","object code:"+uriType);
            return cursor;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //TODO verifier uri si necessaire rappel dans chaque switch
        int uriType = uriMatcher.match(uri);
        long id;
        try {
            switch (uriType){
                case CurrencySql.CODE:
                    id = db.insertOrThrow(CurrencySql.CurrencyTable.TABLE_NAME, null, values);
                    uri = Uri.parse(CurrencySql.URI + Long.toString(id));
                    break;
                case BlockUdSql.CODE:
                    id = db.insertOrThrow(BlockUdSql.BlockTable.TABLE_NAME,null,values);
                    uri = Uri.parse(BlockUdSql.URI + Long.toString(id));
                    break;
                case ContactSql.CODE:
                    id = db.insertOrThrow(ContactSql.ContactTable.TABLE_NAME,null,values);
                    uri = Uri.parse(ContactSql.URI + Long.toString(id));
                    break;
                case EndpointSql.CODE:
                    id = db.insertOrThrow(EndpointSql.EndpointTable.TABLE_NAME,null,values);
                    uri = Uri.parse(EndpointSql.URI + Long.toString(id));
                    break;
                case IdentitySql.CODE:
                    id = db.insertOrThrow(IdentitySql.IdentityTable.TABLE_NAME,null,values);
                    uri = Uri.parse(IdentitySql.URI + Long.toString(id));
                    break;
                case PeerSql.CODE:
                    id = db.insertOrThrow(PeerSql.PeerTable.TABLE_NAME,null,values);
                    uri = Uri.parse(PeerSql.URI + Long.toString(id));
                    break;
                case RequirementSql.CODE:
                    id = db.insertOrThrow(RequirementSql.RequirementTable.TABLE_NAME,null,values);
                    uri = Uri.parse(RequirementSql.URI + Long.toString(id));
                    break;
                case CertificationSql.CODE:
                    id = db.insertOrThrow(CertificationSql.CertificationTable.TABLE_NAME,null,values);
                    uri = Uri.parse(CertificationSql.URI + Long.toString(id));
                    break;
                case TxSql.CODE:
                    id = db.insertOrThrow(TxSql.TxTable.TABLE_NAME,null,values);
                    uri = Uri.parse(TxSql.URI + Long.toString(id));
                    break;
                case WalletSql.CODE:
                    id = db.insertOrThrow(WalletSql.WalletTable.TABLE_NAME,null,values);
                    uri = Uri.parse(WalletSql.URI + Long.toString(id));
                    break;
                case SourceSql.CODE:
                    id = db.insertOrThrow(SourceSql.SourceTable.TABLE_NAME,null,values);
                    uri = Uri.parse(SourceSql.URI + Long.toString(id));
                    break;
                default:
                    id = -2;
                    break;
            }
            if(id == -1){
                throw new RuntimeException(String.format(
                        "%s : Failed to insert [%s] for unknown reasons.",
                        "DbProvider", values, uri));
            }else if(id == -2){
                throw new RuntimeException(String.format(
                        "Unknown URI: [%s]", uri));
            }else{
                Log.d("DB INSERT","object code:"+uriType+" index:"+id);
                notifyChange(uriType);
                return uri;
                //ContentUris.withAppendedId(uri, id);
            }
        } finally {
//            db.close();
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        int deletedRows;
        try {
            switch (uriType){
                case CurrencySql.CODE:
                    deletedRows = db.delete(CurrencySql.CurrencyTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case BlockUdSql.CODE:
                    deletedRows = db.delete(BlockUdSql.BlockTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case ContactSql.CODE:
                    deletedRows = db.delete(ContactSql.ContactTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case EndpointSql.CODE:
                    deletedRows = db.delete(EndpointSql.EndpointTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case IdentitySql.CODE:
                    deletedRows = db.delete(IdentitySql.IdentityTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case PeerSql.CODE:
                    deletedRows = db.delete(PeerSql.PeerTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case RequirementSql.CODE:
                    deletedRows = db.delete(RequirementSql.RequirementTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case CertificationSql.CODE:
                    deletedRows = db.delete(CertificationSql.CertificationTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case TxSql.CODE:
                    deletedRows = db.delete(TxSql.TxTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case WalletSql.CODE:
                    deletedRows = db.delete(WalletSql.WalletTable.TABLE_NAME, selection, selectionArgs);
                    break;
                case SourceSql.CODE:
                    deletedRows = db.delete(SourceSql.SourceTable.TABLE_NAME, selection, selectionArgs);
                    break;
                default:
                    deletedRows = -1;
                    break;
            }
            if (deletedRows==-1){
                throw new RuntimeException(String.format(
                        "Unknown URI: [%s]", uri));
            }else{
                Log.d("DB DELETE","object code:"+uriType+" index:"+(selectionArgs==null?"all":selectionArgs[0]));
                notifyChange(uriType);
                return deletedRows;
            }
        } finally {
//            db.close();
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int uriType = uriMatcher.match(uri);
        int updatedRows;
        try {
            switch (uriType){
                case CurrencySql.CODE:
                    updatedRows = db.update(CurrencySql.CurrencyTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case BlockUdSql.CODE:
                    updatedRows = db.update(BlockUdSql.BlockTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case ContactSql.CODE:
                    updatedRows = db.update(ContactSql.ContactTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case EndpointSql.CODE:
                    updatedRows = db.update(EndpointSql.EndpointTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case IdentitySql.CODE:
                    updatedRows = db.update(IdentitySql.IdentityTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case PeerSql.CODE:
                    updatedRows = db.update(PeerSql.PeerTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case RequirementSql.CODE:
                    updatedRows = db.update(RequirementSql.RequirementTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case CertificationSql.CODE:
                    updatedRows = db.update(CertificationSql.CertificationTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case TxSql.CODE:
                    updatedRows = db.update(TxSql.TxTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case WalletSql.CODE:
                    updatedRows = db.update(WalletSql.WalletTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                case SourceSql.CODE:
                    updatedRows = db.update(SourceSql.SourceTable.TABLE_NAME,values, selection, selectionArgs);
                    break;
                default:
                    updatedRows = -1;
                    break;
            }
            if (updatedRows==-1){
                throw new RuntimeException(String.format(
                        "Unknown URI: [%s]", uri));
            }else{
                Log.d("DB UPDATE","object code:"+uriType+" index:"+selectionArgs[0]);
                notifyChange(uriType);
                return updatedRows;
            }
        } finally {
//            db.close();
        }
    }

    public void notifyChange(int uriType){
        switch (uriType){
            case CurrencySql.CODE:
                getContext().getContentResolver().notifyChange(CurrencySql.URI, null);
                break;
            case BlockUdSql.CODE:
                getContext().getContentResolver().notifyChange(BlockUdSql.URI, null);
                getContext().getContentResolver().notifyChange(WalletSql.URI, null);
                break;
            case ContactSql.CODE:
                getContext().getContentResolver().notifyChange(ContactSql.URI, null);
                break;
            case EndpointSql.CODE:
                getContext().getContentResolver().notifyChange(EndpointSql.URI, null);
                break;
            case IdentitySql.CODE:
                getContext().getContentResolver().notifyChange(IdentitySql.URI, null);
                getContext().getContentResolver().notifyChange(WalletSql.URI, null);
                break;
            case PeerSql.CODE:
                getContext().getContentResolver().notifyChange(PeerSql.URI, null);
                break;
            case RequirementSql.CODE:
                getContext().getContentResolver().notifyChange(RequirementSql.URI, null);
                notifyChange(IdentitySql.CODE);
                break;
            case CertificationSql.CODE:
                getContext().getContentResolver().notifyChange(CertificationSql.URI, null);
                getContext().getContentResolver().notifyChange(ViewCertificationAdapter.URI, null);
                notifyChange(IdentitySql.CODE);
                break;
            case TxSql.CODE:
                getContext().getContentResolver().notifyChange(TxSql.URI, null);
                getContext().getContentResolver().notifyChange(ViewTxAdapter.URI, null);
                notifyChange(WalletSql.CODE);
                break;
            case WalletSql.CODE:
                getContext().getContentResolver().notifyChange(WalletSql.URI, null);
                getContext().getContentResolver().notifyChange(ViewWalletIdentityAdapter.URI, null);
                getContext().getContentResolver().notifyChange(ViewWalletAdapter.URI, null);
                getContext().getContentResolver().notifyChange(ViewTxAdapter.URI, null);
                break;
            case SourceSql.CODE:
                getContext().getContentResolver().notifyChange(SourceSql.URI, null);
                notifyChange(WalletSql.CODE);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI type: " + uriType);
        }
    }
}
