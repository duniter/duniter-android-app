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

        TextView mUd0 = (TextView) view.findViewById(R.id.ud0);
        TextView mSigDelay = (TextView) view.findViewById(R.id.sig_delay);
        TextView mSigValidity = (TextView) view.findViewById(R.id.sig_validity);
        TextView mSigQty = (TextView) view.findViewById(R.id.sig_qty);
        TextView mSigWoT = (TextView) view.findViewById(R.id.sig_woT);
        TextView mMsValidity = (TextView) view.findViewById(R.id.ms_validity);
        TextView mStepMax = (TextView) view.findViewById(R.id.step_max);
        TextView mMedianTimeBlocks = (TextView) view.findViewById(R.id.median_time_blocks);
        TextView mAvgGenTime = (TextView) view.findViewById(R.id.avg_gen_time);
        TextView mDtDiffEval = (TextView) view.findViewById(R.id.dt_diff_eval);
        TextView mBlocksRot = (TextView) view.findViewById(R.id.blocks_rot);
        TextView mPercentRot = (TextView) view.findViewById(R.id.percent_rot);

        TextView growth = (TextView) view.findViewById(R.id.growth);
        growth.setText(getString(R.string.growth) + ": " +
                Double.toString(parameter.getC()) + " / " + Integer.toString(parameter.getDt()));


        mUd0.setText(Long.toString(parameter.getUd0()));
        mSigDelay.setText(Integer.toString(parameter.getSigDelay()));
        mSigValidity.setText(Integer.toString(parameter.getSigValidity()));
        mSigQty.setText(Integer.toString(parameter.getSigQty()));
        mSigWoT.setText(Integer.toString(parameter.getSigWoT()));
        mMsValidity.setText(Integer.toString(parameter.getMsValidity()));
        mStepMax.setText(Integer.toString(parameter.getStepMax()));
        mMedianTimeBlocks.setText(Integer.toString(parameter.getMedianTimeBlocks()));
        mAvgGenTime.setText(Integer.toString(parameter.getAvgGenTime()));
        mDtDiffEval.setText(Integer.toString(parameter.getDtDiffEval()));
        mBlocksRot.setText(Integer.toString(parameter.getBlocksRot()));
        mPercentRot.setText(Double.toString(parameter.getPercentRot()));
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
