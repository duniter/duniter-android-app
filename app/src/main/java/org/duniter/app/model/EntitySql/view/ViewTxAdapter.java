package org.duniter.app.model.EntitySql.view;

import android.net.Uri;
import android.provider.BaseColumns;

import org.duniter.app.model.EntitySql.BlockUdSql.BlockTable;
import org.duniter.app.model.EntitySql.CurrencySql.CurrencyTable;
import org.duniter.app.model.EntitySql.TxSql.TxTable;

/**
 * Created by naivalf27 on 27/04/16.
 */
public class ViewTxAdapter implements BaseColumns, ViewInterface {
    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(ViewTxAdapter.VIEW_NAME+"/").build();
    public static final int CODE = 81;


    public static final String VIEW_NAME = "view_tx_adapter";
    public static final String WALLET_ID = "wallet_id";
    public static final String PUBLIC_KEY = "public_key";
    public static final String UID = "uid";
    public static final String AMOUNT = "amount";
    public static final String COMMENT = "comment";
    public static final String TIME = "time";

    public static final String CURRENCY_ID = "currency_id";
    public static final String CURRENCY_NAME = "currency_name";
    public static final String DT = "dt";
    public static final String LAST_UD = "last_ud";
    public static final String FIRST_UD = "first_ud";

    public static String getCreation() {
        return "CREATE VIEW " + VIEW_NAME +
                " AS SELECT " +
                TxTable.TABLE_NAME + DOT + TxTable._ID + AS + _ID + COMMA +
                TxTable.TABLE_NAME + DOT + TxTable.WALLET_ID + AS + WALLET_ID + COMMA +
                TxTable.TABLE_NAME + DOT + TxTable.PUBLIC_KEY + AS + PUBLIC_KEY + COMMA +
                TxTable.TABLE_NAME + DOT + TxTable.UID + AS + UID + COMMA +
                TxTable.TABLE_NAME + DOT + TxTable.AMOUNT + AS + AMOUNT + COMMA +
                TxTable.TABLE_NAME + DOT + TxTable.TIME + AS + TIME + COMMA +
                TxTable.TABLE_NAME + DOT + TxTable.COMMENT + AS + COMMENT + COMMA +

                CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + AS + CURRENCY_ID + COMMA +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable.NAME + AS + CURRENCY_NAME + COMMA +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable.DT + AS + DT + COMMA +

                " CASE WHEN " + BlockTable.TABLE_NAME + DOT + BlockTable.DIVIDEND  + " IS NULL" +
                " THEN " + CurrencyTable.TABLE_NAME + DOT + CurrencyTable.UD0 +
                " ELSE " + BlockTable.TABLE_NAME + DOT + BlockTable.DIVIDEND + " END" + AS + LAST_UD + COMMA +

                "(SELECT " + BlockTable.TABLE_NAME + DOT + BlockTable.DIVIDEND +
                FROM + BlockTable.TABLE_NAME +
                WHERE + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + "=" +
                "(SELECT MAX(" + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + ") " +
                FROM + BlockTable.TABLE_NAME + WHERE + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + "<=" +
                TxTable.TABLE_NAME + DOT + TxTable.BLOCK_NUMBER + " )"+
                " )" + AS + FIRST_UD + COMMA +

                "(SELECT MAX(" + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + ") " +
                FROM + BlockTable.TABLE_NAME + WHERE + BlockTable.TABLE_NAME + DOT + BlockTable.CURRENCY_ID + "=" +
                CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + " )" + AS + "number_block" +

                FROM + TxTable.TABLE_NAME +

                LEFT_JOIN + CurrencyTable.TABLE_NAME +
                ON + CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID + "=" + TxTable.TABLE_NAME + DOT + TxTable.CURRENCY_ID +

                LEFT_JOIN + BlockTable.TABLE_NAME +
                ON + BlockTable.TABLE_NAME + DOT + BlockTable.CURRENCY_ID + "=" + CurrencyTable.TABLE_NAME + DOT + CurrencyTable._ID +
                AND + BlockTable.TABLE_NAME + DOT + BlockTable.NUMBER + "=" + "number_block";
    }
}
