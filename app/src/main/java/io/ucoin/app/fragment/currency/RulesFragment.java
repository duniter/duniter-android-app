package io.ucoin.app.fragment.currency;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.sqlite.SQLiteTable;

public class RulesFragment extends Fragment {

    private final int SECONDS_IN_DAY = 24 * 60 * 60;
    private static Long mCurrencyId;

    public static RulesFragment newInstance(Long currencyId) {
        RulesFragment fragment = new RulesFragment();
        mCurrencyId = currencyId;
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_currency_rules,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bind(view);
    }



    protected void bind(View view) {
        Currency currency = new Currency(getActivity(), mCurrencyId);
        // Get views

        // growth
        {
            TextView growth = (TextView) view.findViewById(R.id.growth);
            float cPercent = 0;
            cPercent = currency.c() * 100;
            Double dtDays = (double)currency.dt() / SECONDS_IN_DAY;
            growth.setText(getString(R.string.rules_c_and_dt_value, cPercent, dtDays, currency.dt()));
        }

        TextView mUd0 = (TextView) view.findViewById(R.id.ud0);
        mUd0.setText(getString(R.string.rules_ud0_value, currency.ud0()));

        TextView mSigDelay = (TextView) view.findViewById(R.id.sig_delay);
        mSigDelay.setText(getStringFromSeconds(currency.sigDelay()));

        TextView mSigValidity = (TextView) view.findViewById(R.id.sig_validity);
        mSigValidity.setText(getStringFromSeconds(currency.sigValidity()));

        TextView mSigQty = (TextView) view.findViewById(R.id.sig_qty);
        mSigQty.setText(getString(R.string.rules_sig_qty_value, currency.sigQty()));

        TextView mSigWoT = (TextView) view.findViewById(R.id.sig_woT);
        mSigWoT.setText(Integer.toString(currency.sigWoT()));

        TextView mMsValidity = (TextView) view.findViewById(R.id.ms_validity);
        mMsValidity.setText(getStringFromSeconds(currency.msValidity()));

        TextView mStepMax = (TextView) view.findViewById(R.id.step_max);
        mStepMax.setText(Integer.toString(currency.stepMax()));

        TextView mMedianTimeBlocks = (TextView) view.findViewById(R.id.median_time_blocks);
        mMedianTimeBlocks.setText(getString(R.string.rules_median_time_blocks_value, currency.medianTimeBlocks()));

        TextView mAvgGenTime = (TextView) view.findViewById(R.id.avg_gen_time);
        mAvgGenTime.setText(getStringFromSeconds(currency.avgGenTime()));

        TextView mDtDiffEval = (TextView) view.findViewById(R.id.dt_diff_eval);
        mDtDiffEval.setText(getString(R.string.rules_dt_diff_eval_value, currency.dtDiffEval()));

        TextView mBlocksRot = (TextView) view.findViewById(R.id.blocks_rot);
        mBlocksRot.setText(getString(R.string.rules_blocks_rot_value, currency.blocksRot()));

        TextView mPercentRot = (TextView) view.findViewById(R.id.percent_rot);
        mPercentRot.setText(getString(R.string.rules_percent_rot_value, currency.percentRot()));
    }

    /* -- protected methods -- */

    protected String getStringFromSeconds(int seconds) {
        // One day or more :
        if (seconds >= SECONDS_IN_DAY) {
            double days = (double) seconds / SECONDS_IN_DAY;
            return getString(R.string.rules_value_days_and_seconds, days, seconds);
        }

        // Less than a day :
        double minutesPart = (double) seconds / 60;
        int secondsPart = seconds - (int)minutesPart * 60;
        return getString(R.string.rules_value_minutes_and_seconds, minutesPart, secondsPart, seconds);
    }

    public class ViewHolder{
        public final int INDEX_NAME;
        public final int INDEX_C;
        public final int INDEX_DT;
        public final int INDEX_UD0;
        public final int INDEX_SIGDELAY;
        public final int INDEX_SIGVALIDITY;
        public final int INDEX_SIGQTY;
        public final int INDEX_SIGWOT;
        public final int INDEX_MSVALIDITY;
        public final int INDEX_STEPMAX;
        public final int INDEX_MEDIANTIMEBLOCKS;
        public final int INDEX_AVGGENTIME;
        public final int INDEX_DTDIFFEVAL;
        public final int INDEX_BLOCKSROT;
        public final int INDEX_PERCENTROT;

        public ViewHolder(Cursor cursor) {
            INDEX_NAME = cursor.getColumnIndex(SQLiteTable.Currency.NAME);
            INDEX_C = cursor.getColumnIndex(SQLiteTable.Currency.C);
            INDEX_DT = cursor.getColumnIndex(SQLiteTable.Currency.DT);
            INDEX_UD0 = cursor.getColumnIndex(SQLiteTable.Currency.UD0);
            INDEX_SIGDELAY = cursor.getColumnIndex(SQLiteTable.Currency.SIGDELAY);
            INDEX_SIGVALIDITY = cursor.getColumnIndex(SQLiteTable.Currency.SIGVALIDITY);
            INDEX_SIGQTY = cursor.getColumnIndex(SQLiteTable.Currency.SIGQTY);
            INDEX_SIGWOT = cursor.getColumnIndex(SQLiteTable.Currency.SIGWOT);
            INDEX_MSVALIDITY = cursor.getColumnIndex(SQLiteTable.Currency.MSVALIDITY);
            INDEX_STEPMAX = cursor.getColumnIndex(SQLiteTable.Currency.STEPMAX);
            INDEX_MEDIANTIMEBLOCKS = cursor.getColumnIndex(SQLiteTable.Currency.MEDIANTIMEBLOCKS);
            INDEX_AVGGENTIME = cursor.getColumnIndex(SQLiteTable.Currency.AVGGENTIME);
            INDEX_DTDIFFEVAL = cursor.getColumnIndex(SQLiteTable.Currency.DTDIFFEVAL);
            INDEX_BLOCKSROT = cursor.getColumnIndex(SQLiteTable.Currency.BLOCKSROT);
            INDEX_PERCENTROT = cursor.getColumnIndex(SQLiteTable.Currency.PERCENTROT);
        }
    }

}
