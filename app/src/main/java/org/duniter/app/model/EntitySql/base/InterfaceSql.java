package org.duniter.app.model.EntitySql.base;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by naivalf27 on 20/04/16.
 */
public interface InterfaceSql<T> {

    public T getById(long id);
    public long insert(T entity);
    public Cursor query(String selection,String[] selectionArgs,String orderBy);
    public Cursor query(String selection,String[] selectionArgs);
    public int delete(long id);
    public int update(T entity,long id);

    public String getCreation();
    public T fromCursor(Cursor cursor);
    public ContentValues toContentValues(T entity);
}
