package org.duniter.app.view.currency;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.view.MainActivity;

public class RulesFragment extends Fragment {

    private final int SECONDS_IN_DAY = 24 * 60 * 60;
    private static Long mCurrencyId;
    private static Currency currency;

    public static RulesFragment newInstance(Currency _currency) {
        currency = _currency;
        RulesFragment fragment = new RulesFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }


//    public RulesFragment(Currency currency){
//        this.currency = currency;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
        }
        getActivity().setTitle(getActivity().getString(R.string.list_rules));

        return inflater.inflate(R.layout.fragment_currency_rules,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        bind(view);
    }



    protected void bind(View view) {
        // Get views

        // growth
        {
            TextView growth = (TextView) view.findViewById(R.id.growth);
            float cPercent = 0;
            cPercent = currency.getC() * 100;
            Double dtDays = (double)currency.getDt()/ SECONDS_IN_DAY;
            growth.setText(getString(R.string.rules_c_and_dt_value, cPercent, dtDays, currency.getDt()));
        }

        TextView mUd0 = (TextView) view.findViewById(R.id.ud0);
        mUd0.setText(getString(R.string.rules_ud0_value, currency.getUd0()));

        TextView mSigDelay = (TextView) view.findViewById(R.id.sig_delay);
        mSigDelay.setText(getStringFromSeconds(currency.getSigPeriod()));

        TextView mSigValidity = (TextView) view.findViewById(R.id.sig_validity);
        mSigValidity.setText(getStringFromSeconds(currency.getSigValidity()));

        TextView mSigQty = (TextView) view.findViewById(R.id.sig_qty);
        mSigQty.setText(getString(R.string.rules_sig_qty_value, currency.getSigQty()));

        TextView mSigWoT = (TextView) view.findViewById(R.id.sig_woT);
        //mSigWoT.setText(Integer.toString(currency.sigWoT()));
        mSigWoT.setText("inconnue");

        TextView mMsValidity = (TextView) view.findViewById(R.id.ms_validity);
        mMsValidity.setText(getStringFromSeconds(currency.getMsValidity()));

        TextView mStepMax = (TextView) view.findViewById(R.id.step_max);
        mStepMax.setText(String.valueOf(currency.getStepMax()));

        TextView mMedianTimeBlocks = (TextView) view.findViewById(R.id.median_time_blocks);
        mMedianTimeBlocks.setText(getString(R.string.rules_median_time_blocks_value, currency.getMedianTimeBlocks()));

        TextView mAvgGenTime = (TextView) view.findViewById(R.id.avg_gen_time);
        mAvgGenTime.setText(getStringFromSeconds(currency.getAvgGenTime()));

        TextView mDtDiffEval = (TextView) view.findViewById(R.id.dt_diff_eval);
        mDtDiffEval.setText(getString(R.string.rules_dt_diff_eval_value, currency.getDtDiffEval()));

        TextView mBlocksRot = (TextView) view.findViewById(R.id.blocks_rot);
        mBlocksRot.setText(getString(R.string.rules_blocks_rot_value, currency.getBlocksRot()));

        TextView mPercentRot = (TextView) view.findViewById(R.id.percent_rot);
        mPercentRot.setText(getString(R.string.rules_percent_rot_value, currency.getPercentRot()));
    }

    /* -- protected methods -- */

    protected String getStringFromSeconds(long seconds) {
        // One day or more :
        if (seconds >= SECONDS_IN_DAY) {
            double days = (double) seconds / SECONDS_IN_DAY;
            return getString(R.string.rules_value_days_and_seconds, days, seconds);
        }

        // Less than a day :
        double minutesPart = (double) seconds / 60;
        long secondsPart = seconds - (long)minutesPart * 60;
        return getString(R.string.rules_value_minutes_and_seconds, minutesPart, secondsPart, seconds);
    }

}
