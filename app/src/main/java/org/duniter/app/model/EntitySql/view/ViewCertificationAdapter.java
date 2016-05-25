package org.duniter.app.model.EntitySql.view;

import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.EntitySql.CertificationSql.CertificationTable;
import org.duniter.app.model.EntitySql.CurrencySql.CurrencyTable;
import org.duniter.app.model.EntitySql.IdentitySql.IdentityTable;

/**
 * Created by naivalf27 on 27/04/16.
 */
public class ViewCertificationAdapter implements BaseColumns, ViewInterface {
    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(ViewCertificationAdapter.VIEW_NAME+"/").build();
    public static final int CODE = 111;


    public static final String VIEW_NAME = "view_certification_adapter";

    public static final String PUBLIC_KEY = "public_key";
    public static final String TYPE = "type";
    public static final String UID = "uid";
    public static final String MEDIAN_TIME = "median_time";
    public static final String IDENTITY_ID = "identity_id";
    public static final String SIG_VALIDITY = "sig_validity";
    public static final String BLOCK_NUMBER = "block_number";

    public static String getCreation() {
        return "CREATE VIEW " + VIEW_NAME +
                " AS SELECT " +
                CertificationTable.TABLE_NAME + DOT + CertificationTable._ID + AS + _ID + COMMA +
                CertificationTable.TABLE_NAME + DOT + CertificationTable.PUBLIC_KEY + AS + PUBLIC_KEY + COMMA +
                CertificationTable.TABLE_NAME + DOT + CertificationTable.UID + AS + UID + COMMA +
                CertificationTable.TABLE_NAME + DOT + CertificationTable.MEDIAN_TIME + AS + MEDIAN_TIME + COMMA +
                CertificationTable.TABLE_NAME + DOT + CertificationTable.BLOCK_NUMBER + AS + BLOCK_NUMBER + COMMA +
                CertificationTable.TABLE_NAME + DOT + CertificationTable.IDENTITY_ID + AS + IDENTITY_ID + COMMA +
                CertificationTable.TABLE_NAME + DOT + CertificationTable.TYPE + AS + TYPE + COMMA +

                CurrencyTable.TABLE_NAME + DOT + CurrencyTable.SIGVALIDITY + AS + SIG_VALIDITY +

                FROM + CertificationTable.TABLE_NAME +

                LEFT_JOIN + IdentityTable.TABLE_NAME +
                ON + IdentityTable.TABLE_NAME + DOT + IdentityTable._ID + "=" + CertificationTable.TABLE_NAME + DOT + CertificationTable.IDENTITY_ID +

                LEFT_JOIN + CurrencyTable.TABLE_NAME +
                ON + CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + "=" + IdentityTable.TABLE_NAME + DOT + IdentityTable.CURRENCY_ID;
    }
}
