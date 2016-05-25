package org.duniter.app.model.EntitySql.view;

import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.EntitySql.BlockUdSql.BlockTable;
import org.duniter.app.model.EntitySql.CurrencySql.CurrencyTable;
import org.duniter.app.model.EntitySql.IdentitySql.IdentityTable;
import org.duniter.app.model.EntitySql.RequirementSql.RequirementTable;
import org.duniter.app.model.EntitySql.WalletSql.WalletTable;

/**
 * Created by naivalf27 on 27/04/16.
 */
public class ViewWalletIdentityAdapter implements BaseColumns, ViewInterface {
    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(ViewWalletIdentityAdapter.VIEW_NAME+"/").build();
    public static final int CODE = 92;


    public static final String VIEW_NAME = "view_wallet_identity_adapter";

    public static final String PUBLIC_KEY = "public_key";
    public static final String AMOUNT = "amount";
    public static final String ALIAS = "alias";
    public static final String CURRENCY_NAME = "currency_name";
    public static final String SIG_NEED_QTY = "sig_need_qty";
    public static final String SIG_DATE = "sig_date";
    public static final String CURRENCY_ID = "currency_id";
    public static final String DT = "dt";
    public static final String IDENTITY_ID = "identity_id";
    public static final String LAST_UD = "last_ud";

    public static final String NB_REQUIREMENTS = "nb_requirements";
    public static final String MEMBERSHIP = "membership_expire_in";
    public static final String MEMBERSHIP_PENDING = "membership_pending_expire_in";

    public static String getCreation() {
        return "CREATE VIEW " + VIEW_NAME +
                " AS SELECT " +
                WalletTable.TABLE_NAME + DOT + WalletTable._ID + AS + _ID + COMMA +
                WalletTable.TABLE_NAME + DOT + WalletTable.PUBLIC_KEY + AS + PUBLIC_KEY + COMMA +
                WalletTable.TABLE_NAME + DOT + WalletTable.AMOUNT + AS + AMOUNT + COMMA +
                WalletTable.TABLE_NAME + DOT + WalletTable.ALIAS + AS + ALIAS + COMMA +

                CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + AS + CURRENCY_ID + COMMA +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable.NAME + AS + CURRENCY_NAME + COMMA +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable.DT + AS + DT + COMMA +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable.SIGQTY + AS + SIG_NEED_QTY + COMMA +

                IdentityTable.TABLE_NAME + DOT + IdentityTable._ID + AS + IDENTITY_ID + COMMA +
                IdentityTable.TABLE_NAME + DOT + IdentityTable.SIG_DATE + AS + SIG_DATE + COMMA +

                RequirementTable.TABLE_NAME + DOT + RequirementTable.NUMBER_CERTIFICATION + AS +NB_REQUIREMENTS + COMMA +
                RequirementTable.TABLE_NAME + DOT + RequirementTable.MEMBERSHIP_EXPIRES_IN + AS + MEMBERSHIP + COMMA +
                RequirementTable.TABLE_NAME + DOT + RequirementTable.MEMBERSHIP_PENDING_EXPIRES_IN + AS + MEMBERSHIP_PENDING + COMMA +

                " CASE WHEN " + BlockTable.TABLE_NAME + DOT + BlockTable.DIVIDEND  + " IS NULL" +
                " THEN " + CurrencyTable.TABLE_NAME + DOT + CurrencyTable.UD0 +
                " ELSE " + BlockTable.TABLE_NAME + DOT + BlockTable.DIVIDEND + " END" + AS + LAST_UD + COMMA +

                "(SELECT MAX(" + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + ") " +
                FROM + BlockTable.TABLE_NAME + WHERE + BlockTable.TABLE_NAME + DOT + BlockTable.CURRENCY_ID + "=" +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + " )" + AS + "number_block" +

                FROM + WalletTable.TABLE_NAME +

                LEFT_JOIN + CurrencyTable.TABLE_NAME +
                ON + CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + "=" + WalletTable.TABLE_NAME + DOT + WalletTable.CURRENCY_ID +

                LEFT_JOIN + IdentityTable.TABLE_NAME +
                ON + IdentityTable.TABLE_NAME + DOT + IdentityTable.WALLET_ID + "=" + WalletTable.TABLE_NAME + DOT + WalletTable._ID +

                LEFT_JOIN + RequirementTable.TABLE_NAME +
                ON + RequirementTable.TABLE_NAME + DOT + RequirementTable.IDENTITY_ID + "=" + IdentityTable.TABLE_NAME + DOT + IdentityTable._ID +

                LEFT_JOIN + BlockTable.TABLE_NAME +
                ON + BlockTable.TABLE_NAME + DOT + BlockTable.CURRENCY_ID + "=" + CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID +
                AND + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + "=" + "number_block";
    }
}
