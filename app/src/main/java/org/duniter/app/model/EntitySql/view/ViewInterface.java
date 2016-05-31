package org.duniter.app.model.EntitySql.view;

/**
 * Created by naivalf27 on 27/04/16.
 */
public interface ViewInterface {
    String AUTHORITY = "org.duniter.app.services.dbprovider";

    String INTEGER = " INTEGER ";
    String REAL    = " REAL ";
    String TEXT    = " TEXT ";
    String UNIQUE  = " UNIQUE ";
    String NOTNULL = " NOT NULL ";
    String COMMA   = ", ";
    String AS      = " AS ";
    String DOT     = ".";

    String LEFT_JOIN = " LEFT JOIN ";
    String FROM      = " FROM ";
    String ON        = " ON ";
    String WHERE     = " WHERE ";
    String AND       = " AND ";
}
