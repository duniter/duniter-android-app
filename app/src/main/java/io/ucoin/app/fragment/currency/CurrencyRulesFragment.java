package io.ucoin.app.fragment.currency;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Currency;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

public class CurrencyRulesFragment extends Fragment {

    private final int SECONDS_IN_DAY = 24 * 60 * 60;

    private ProgressViewAdapter mProgressViewAdapter;

    public static CurrencyRulesFragment newInstance(Currency parameter) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Currency.class.getSimpleName(), parameter);

        CurrencyRulesFragment fragment = new CurrencyRulesFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    public static CurrencyRulesFragment newInstance(BlockchainParameter parameter) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(BlockchainParameter.class.getSimpleName(), parameter);

        CurrencyRulesFragment fragment = new CurrencyRulesFragment();
        fragment.setArguments(newInstanceArgs);
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

        Bundle newInstanceArgs = getArguments();
        BlockchainParameter parameter = (BlockchainParameter) newInstanceArgs
                .getSerializable(BlockchainParameter.class.getSimpleName());

        // Load progress
        mProgressViewAdapter = new ProgressViewAdapter(view,
                R.id.load_progress,
                R.id.wrap_content
                );

        if (parameter != null) {
            bind(view, parameter);
        }
        else {
            Currency currency = (Currency) newInstanceArgs
                    .getSerializable(Currency.class.getSimpleName());

            LoadTask loadTask = new LoadTask();
            loadTask.execute(currency.getId());
        }
    }

    protected void bind(View view, BlockchainParameter parameter) {

        // Get views


        // growth
        {
            TextView growth = (TextView) view.findViewById(R.id.growth);
            Double cPercent = parameter.getC() * 100;
            Double dtDays = (double)parameter.getDt() / SECONDS_IN_DAY;
            growth.setText(getString(R.string.c_and_dt_value, cPercent, dtDays, parameter.getDt()));
        }

        TextView mUd0 = (TextView) view.findViewById(R.id.ud0);
        mUd0.setText(getString(R.string.ud0_value, parameter.getUd0()));

        TextView mSigDelay = (TextView) view.findViewById(R.id.sig_delay);
        mSigDelay.setText(getStringFromSeconds(parameter.getSigDelay()));

        TextView mSigValidity = (TextView) view.findViewById(R.id.sig_validity);
        mSigValidity.setText(getStringFromSeconds(parameter.getSigValidity()));

        TextView mSigQty = (TextView) view.findViewById(R.id.sig_qty);
        mSigQty.setText(getString(R.string.sig_qty_value, parameter.getSigQty()));

        TextView mSigWoT = (TextView) view.findViewById(R.id.sig_woT);
        mSigWoT.setText(Integer.toString(parameter.getSigWoT()));

        TextView mMsValidity = (TextView) view.findViewById(R.id.ms_validity);
        mMsValidity.setText(getStringFromSeconds(parameter.getMsValidity()));

        TextView mStepMax = (TextView) view.findViewById(R.id.step_max);
        mStepMax.setText(Integer.toString(parameter.getStepMax()));

        TextView mMedianTimeBlocks = (TextView) view.findViewById(R.id.median_time_blocks);
        mMedianTimeBlocks.setText(getString(R.string.median_time_blocks_value, parameter.getMedianTimeBlocks()));

        TextView mAvgGenTime = (TextView) view.findViewById(R.id.avg_gen_time);
        mAvgGenTime.setText(getStringFromSeconds(parameter.getAvgGenTime()));

        TextView mDtDiffEval = (TextView) view.findViewById(R.id.dt_diff_eval);
        mDtDiffEval.setText(getString(R.string.dt_diff_eval_value, parameter.getDtDiffEval()));

        TextView mBlocksRot = (TextView) view.findViewById(R.id.blocks_rot);
        mBlocksRot.setText(getString(R.string.blocks_rot_value, parameter.getBlocksRot()));

        TextView mPercentRot = (TextView) view.findViewById(R.id.percent_rot);
        mPercentRot.setText(getString(R.string.percent_rot_value, parameter.getPercentRot()));
    }

    /* -- protected methods -- */

    protected String getStringFromSeconds(int seconds) {
        // One day or more :
        if (seconds >= SECONDS_IN_DAY) {
            double days = (double) seconds / SECONDS_IN_DAY;
            return getString(R.string.value_days_and_seconds, days, seconds);
        }

        // Less than a day :
        double minutesPart = (double) seconds / 60;
        int secondsPart = seconds - (int)minutesPart * 60;
        return getString(R.string.value_minutes_and_seconds, minutesPart, secondsPart, seconds);
    }


    public class LoadTask extends AsyncTaskHandleException<Long, Void, BlockchainParameter> {

        public LoadTask() {
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected BlockchainParameter  doInBackgroundHandleException(Long... currencyIds) {
            // Refresh the membership data
            BlockchainRemoteService bcService = ServiceLocator.instance().getBlockchainRemoteService();
            return bcService.getParameters(currencyIds[0]);
        }

        @Override
        protected void onSuccess(BlockchainParameter parameters) {
            bind(getView(), parameters);
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }


}
