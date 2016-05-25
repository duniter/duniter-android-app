package org.duniter.app.model.EntitySql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntitySql.base.AbstractSql;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class CurrencySql extends AbstractSql<Currency> {

    public static final Uri URI = new Uri.Builder().scheme("content").authority(AUTHORITY)
            .path(CurrencyTable.TABLE_NAME+"/").build();
    public static final int CODE = 10;


    public CurrencySql(Context context) {
        super(context,URI);
    }

    public List<Currency> getAllCurrency() {
        List<Currency> currencyList = new ArrayList<>();
        Cursor cursor = query(null,null);
        if (cursor.moveToFirst()){
            do {
                currencyList.add(fromCursor(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return currencyList;
    }

    public Currency getByName(String name) {
        Currency currency = null;
        Cursor cursor = query(CurrencyTable.NAME+"=?", new String[]{name});
        if (cursor.moveToFirst()){
            currency = fromCursor(cursor);
        }
        cursor.close();
        return currency;
    }


    /*################################FONCTION DE BASE################################*\
                                    Basic CRUD functions.
    \*################################################################################*/

    @Override
    public String getCreation() {
        return "CREATE TABLE " + CurrencyTable.TABLE_NAME + "(" +
                CurrencyTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA +
                CurrencyTable.NAME + TEXT + NOTNULL + UNIQUE + COMMA +
                CurrencyTable.C + REAL + NOTNULL + COMMA +
                CurrencyTable.DT + INTEGER + NOTNULL + COMMA +
                CurrencyTable.UD0 + INTEGER + NOTNULL + COMMA +
                CurrencyTable.SIGVALIDITY + INTEGER + NOTNULL + COMMA +
                CurrencyTable.SIGQTY + INTEGER + NOTNULL + COMMA +
                CurrencyTable.MSVALIDITY + INTEGER + NOTNULL + COMMA +
                CurrencyTable.STEPMAX + INTEGER + NOTNULL + COMMA +
                CurrencyTable.MEDIANTIMEBLOCKS + INTEGER + NOTNULL + COMMA +
                CurrencyTable.AVGGENTIME + INTEGER + NOTNULL + COMMA +
                CurrencyTable.DTDIFFEVAL + INTEGER + NOTNULL + COMMA +
                CurrencyTable.BLOCKSROT + INTEGER + NOTNULL + COMMA +
                CurrencyTable.PERCENTROT + REAL + NOTNULL + COMMA +
                CurrencyTable.SIGPERIOD + INTEGER + NOTNULL + COMMA +
                CurrencyTable.SIGSTOCK + INTEGER + NOTNULL + COMMA +
                CurrencyTable.SIGWINDOW + INTEGER + NOTNULL + COMMA +
                CurrencyTable.IDTYWINDOW + INTEGER + NOTNULL + COMMA +
                CurrencyTable.MSWINDOW + INTEGER + NOTNULL + COMMA +
                CurrencyTable.XPERCENT + REAL + NOTNULL +
                ")";
    }

    @Override
    public Currency fromCursor(Cursor cursor) {

        int nameIndex = cursor.getColumnIndex(CurrencyTable.NAME);
        int idIndex = cursor.getColumnIndex(CurrencyTable._ID);
        int cIndex = cursor.getColumnIndex(CurrencyTable.C);
        int xpercentIndex = cursor.getColumnIndex(CurrencyTable.XPERCENT);
        int percentRotIndex = cursor.getColumnIndex(CurrencyTable.PERCENTROT);
        int dtIndex = cursor.getColumnIndex(CurrencyTable.DT);
        int ud0Index = cursor.getColumnIndex(CurrencyTable.UD0);
        int sigPeriodIndex = cursor.getColumnIndex(CurrencyTable.SIGPERIOD);
        int sigStockIndex = cursor.getColumnIndex(CurrencyTable.SIGSTOCK);
        int sigWindowIndex = cursor.getColumnIndex(CurrencyTable.SIGWINDOW);
        int sigValidityIndex = cursor.getColumnIndex(CurrencyTable.SIGVALIDITY);
        int sigQtyIndex = cursor.getColumnIndex(CurrencyTable.SIGQTY);
        int idtyWindowIndex = cursor.getColumnIndex(CurrencyTable.IDTYWINDOW);
        int msWindowIndex = cursor.getColumnIndex(CurrencyTable.MSWINDOW);
        int msValidityIndex = cursor.getColumnIndex(CurrencyTable.MSVALIDITY);
        int stepMaxIndex = cursor.getColumnIndex(CurrencyTable.STEPMAX);
        int medianTimeBlocksIndex = cursor.getColumnIndex(CurrencyTable.MEDIANTIMEBLOCKS);
        int avgGenTimeIndex = cursor.getColumnIndex(CurrencyTable.AVGGENTIME);
        int dtDiffEvalIndex = cursor.getColumnIndex(CurrencyTable.DTDIFFEVAL);
        int blocksRotIndex = cursor.getColumnIndex(CurrencyTable.BLOCKSROT);

        Currency currency = new Currency();
        currency.setName(cursor.getString(nameIndex));
        currency.setId(cursor.getLong(idIndex));
        currency.setC(cursor.getFloat(cIndex));
        currency.setXpercent(cursor.getFloat(xpercentIndex));
        currency.setPercentRot(cursor.getFloat(percentRotIndex));
        currency.setDt(cursor.getLong(dtIndex));
        currency.setUd0(cursor.getLong(ud0Index));
        currency.setSigPeriod(cursor.getLong(sigPeriodIndex));
        currency.setSigStock(cursor.getLong(sigStockIndex));
        currency.setSigWindow(cursor.getLong(sigWindowIndex));
        currency.setSigValidity(cursor.getLong(sigValidityIndex));
        currency.setSigQty(cursor.getLong(sigQtyIndex));
        currency.setIdtyWindow(cursor.getLong(idtyWindowIndex));
        currency.setMsWindow(cursor.getLong(msWindowIndex));
        currency.setMsValidity(cursor.getLong(msValidityIndex));
        currency.setStepMax(cursor.getLong(stepMaxIndex));
        currency.setMedianTimeBlocks(cursor.getLong(medianTimeBlocksIndex));
        currency.setAvgGenTime(cursor.getLong(avgGenTimeIndex));
        currency.setDtDiffEval(cursor.getLong(dtDiffEvalIndex));
        currency.setBlocksRot(cursor.getLong(blocksRotIndex));

        return currency;
    }

    @Override
    public ContentValues toContentValues(Currency entity) {
        ContentValues values = new ContentValues();

        values.put(CurrencyTable.NAME, entity.getName());
        values.put(CurrencyTable.C, entity.getC());
        values.put(CurrencyTable.DT, entity.getDt());
        values.put(CurrencyTable.UD0, entity.getUd0());
        values.put(CurrencyTable.SIGPERIOD, entity.getSigPeriod());
        values.put(CurrencyTable.SIGSTOCK, entity.getSigStock());
        values.put(CurrencyTable.SIGWINDOW, entity.getSigWindow());
        values.put(CurrencyTable.SIGVALIDITY, entity.getSigValidity());
        values.put(CurrencyTable.SIGQTY, entity.getSigQty());
        values.put(CurrencyTable.IDTYWINDOW, entity.getIdtyWindow());
        values.put(CurrencyTable.MSWINDOW, entity.getMsWindow());
        values.put(CurrencyTable.XPERCENT, entity.getXpercent());
        values.put(CurrencyTable.MSVALIDITY, entity.getMsValidity());
        values.put(CurrencyTable.STEPMAX, entity.getStepMax());
        values.put(CurrencyTable.MEDIANTIMEBLOCKS, entity.getMedianTimeBlocks());
        values.put(CurrencyTable.AVGGENTIME, entity.getAvgGenTime());
        values.put(CurrencyTable.DTDIFFEVAL, entity.getDtDiffEval());
        values.put(CurrencyTable.BLOCKSROT, entity.getBlocksRot());
        values.put(CurrencyTable.PERCENTROT,    entity.getPercentRot());

        return values;
    }

    public class CurrencyTable implements BaseColumns{
        public static final String TABLE_NAME = "currency";

        public static final String NAME = "name";
        public static final String C = "c";
        public static final String DT = "dt";
        public static final String UD0 = "ud0";
        public static final String SIGVALIDITY = "sig_validity";
        public static final String SIGQTY = "sig_qty";
        public static final String MSVALIDITY = "ms_validity";
        public static final String STEPMAX = "step_max";
        public static final String MEDIANTIMEBLOCKS = "median_time_blocks";
        public static final String AVGGENTIME = "avg_gen_time";
        public static final String DTDIFFEVAL = "dt_diff_eval";
        public static final String BLOCKSROT = "blocks_rot";
        public static final String PERCENTROT = "percent_rot";
        public static final String SIGPERIOD = "sig_period";
        public static final String SIGSTOCK = "sig_stock";
        public static final String SIGWINDOW = "sig_window";
        public static final String IDTYWINDOW = "idty_window";
        public static final String MSWINDOW = "ms_window";
        public static final String XPERCENT = "x_percent";

    }
}
