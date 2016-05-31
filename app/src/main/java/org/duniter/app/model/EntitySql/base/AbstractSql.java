package org.duniter.app.model.EntitySql.base;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by naivalf27 on 20/04/16.
 */
public abstract class AbstractSql<T> implements InterfaceSql<T>{

    public Context context;
    public Uri uri;

    public static final String AUTHORITY = "org.duniter.app.services.dbprovider";

    public AbstractSql(Context context, Uri URI){
        this.context = context;
        this.uri = URI;
    }

    public long getId(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null) {
            try {
                return Long.parseLong(lastPathSegment);
            } catch (NumberFormatException e) {
                Log.e("AbstractSql", "Number Format Exception : " + e);
            }
        }
        return -1;
    }

    @Override
    public T getById(long id) {
        T t = null;
        Cursor cursor = query(BaseColumns._ID +"=?",new String[]{String.valueOf(id)});
        if(cursor.moveToFirst()){
            t = fromCursor(cursor);
        }
        cursor.close();
        return t;
    }

    @Override
    public long insert(T entity) {
        Uri uri = context.getContentResolver().insert(this.uri,toContentValues(entity));
        return getId(uri);
    }

    @Override
    public Cursor query(String selection, String[] selectionArgs, String orderBy) {
        return context.getContentResolver().query(this.uri,null,selection,selectionArgs,orderBy);
    }

    @Override
    public Cursor query(String selection, String[] selectionArgs) {
        return query(selection,selectionArgs,null);
    }

    public int delete(long id) {
        return context.getContentResolver().delete(
                this.uri,BaseColumns._ID + "=?",new String[]{String.valueOf(id)});
    }

    public int update(T entity,long id) {
        return context.getContentResolver().update(
                this.uri,
                toContentValues(entity),
                BaseColumns._ID + "=?",new String[]{String.valueOf(id)});
    }

    public static final String INTEGER = " INTEGER ";
    public static final String REAL    = " REAL ";
    public static final String TEXT    = " TEXT ";
    public static final String UNIQUE  = " UNIQUE ";
    public static final String NOTNULL = " NOT NULL ";
    public static final String COMMA   = ", ";
    public static final String AS      = " AS ";
    public static final String DOT     = ".";
}
