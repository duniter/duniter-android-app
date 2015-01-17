package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;


public class HomeFragment extends Fragment{

    private static final String TAG = "HomeFragment";
    private View mView;

    private TextView mStatusText;
    private ProgressViewAdapter mProgressViewAdapter;
    private ImageView mStatusImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mView = inflater.inflate(R.layout.fragment_home,
                container, false);

        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.app_name));

        // Currency text
        mStatusText = (TextView)view.findViewById(R.id.status_text);

        // Image
        mStatusImage = (ImageView)view.findViewById(R.id.status_image);

        // Progress
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.load_progress),
                mStatusImage);

        // Load currency
        loadCurrency();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    protected void onError(Throwable t) {
        mStatusText.setError(t.getMessage());
    }

    protected void loadCurrency() {
        // TODO detect the first launch (and start the login UI ?)
        final boolean isFirstLaunch = false;

        mStatusText.setText(getString(R.string.connecting_label));
        mProgressViewAdapter.showProgress(true);

        LoadCurrencyTask loadCurrencyTask = new LoadCurrencyTask();
        loadCurrencyTask.execute();
    }


    public class LoadCurrencyTask extends AsyncTaskHandleException<Void, Void, BlockchainParameter> {
        @Override
        protected BlockchainParameter doInBackgroundHandleException(Void... param) {
            DataContext dataContext = ServiceLocator.instance().getDataContext();

            // Load currency
            BlockchainParameter result = ServiceLocator.instance().getBlockchainService().getParameters();
            dataContext.setBlockchainParameter(result);

            // Load default wallet
            Wallet defaultWallet = ServiceLocator.instance().getDataService().getDefaultWallet();
            dataContext.setWallet(defaultWallet);

            return result;
        }

        @Override
        protected void onSuccess(final BlockchainParameter result) {
            mStatusText.setText(Html.fromHtml(getString(R.string.connected_label, result.getCurrency())));
            mStatusImage.setImageResource(R.drawable.world91);
            mStatusImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Display the currency details
                    Toast.makeText(HomeFragment.this.getActivity(),
                            getString(R.string.connection_details,
                                    result.getCurrency(),
                                    result.getUd0(),
                                    result.getDt()),
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            final String errorMessage = getString(R.string.connected_error, t.getMessage());
            Log.e(TAG, errorMessage, t);

            mStatusText.setText(getString(R.string.not_connected_label));
            mStatusImage.setImageResource(R.drawable.warning45);
            mStatusImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Display the error on click
                    Toast.makeText(HomeFragment.this.getActivity(),
                            getString(R.string.connected_error, errorMessage),
                            Toast.LENGTH_LONG)
                            .show();
                }
            });
            mProgressViewAdapter.showProgress(false);

            // Display the error
            Toast.makeText(HomeFragment.this.getActivity(),
                    errorMessage,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
