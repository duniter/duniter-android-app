package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;


public class HomeFragment extends Fragment {

    private TextView mStatusText;
    private ProgressViewAdapter mProgressViewAdapter;
    private ImageView mStatusImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_home,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Currency text
        mStatusText = (TextView) view.findViewById(R.id.status_text);

        // Image
        mStatusImage = (ImageView) view.findViewById(R.id.status_image);

        // Progress
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.load_progress),
                mStatusImage);

        // Load currency
        loadCurrency();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_home, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(getString(R.string.app_name));
        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(getActivity().SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return ((MainActivity) getActivity()).onQueryTextSubmit(searchItem, s);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchView.setIconified(true);
                }
            }
        });
    }

    //Return false to allow normal menu processing to proceed, true to consume it here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    protected void loadCurrency() {
        // TODO detect the first launch (and start the login UI ?)
        //smoul: do this in the onCreate method. It is called once on during fragment creation
        //and as the home fragment is the root fragment of the app it'll be called
        // only once during the whole activity lifecycle
        mStatusText.setText(getString(R.string.connecting_dots));
        mProgressViewAdapter.showProgress(true);

        LoadCurrencyTask loadCurrencyTask = new LoadCurrencyTask();
        loadCurrencyTask.execute();
    }


    public class LoadCurrencyTask extends AsyncTaskHandleException<Void, Void, BlockchainParameter> {
        @Override
        protected BlockchainParameter doInBackgroundHandleException(Void... param) {
            DataContext dataContext = ServiceLocator.instance().getDataContext();
            Wallet currentWallet = dataContext.getWallet();
            BlockchainParameter result = dataContext.getBlockchainParameter();

            if (currentWallet == null || result == null) {
                // Load currency
                result = ServiceLocator.instance().getBlockchainService().getParameters();
                dataContext.setBlockchainParameter(result);

                // Load default wallet
                Wallet defaultWallet = ServiceLocator.instance().getDataService().getDefaultWallet();
                defaultWallet.setCurrency(result.getCurrency());
                dataContext.setWallet(defaultWallet);

                // Load the crypto service (load lib)
                ServiceLocator.instance().getCryptoService();
            }

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
            Log.e(getClass().getSimpleName(), errorMessage, t);

            mStatusText.setText(getString(R.string.not_connected));
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
