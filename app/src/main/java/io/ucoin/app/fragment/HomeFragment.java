package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.technical.AsyncTaskHandleException;


public class HomeFragment extends Fragment {

    private View mStatusPanel;
    private TextView mStatusText;
    private ProgressViewAdapter mProgressViewAdapter;
    private ImageView mStatusImage;

    public static HomeFragment newInstance() {
        return new HomeFragment();
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
        return inflater.inflate(R.layout.fragment_home,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tab host
        TabHost tabs = (TabHost)view.findViewById(R.id.tabHost);
        tabs.setup();
        {
            TabHost.TabSpec spec = tabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator(getString(R.string.wallets));
            tabs.addTab(spec);
        }
        {
            TabHost.TabSpec spec = tabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator(getString(R.string.favorites));
            tabs.addTab(spec);
        }

        mStatusPanel = view.findViewById(R.id.status_panel);
        mStatusPanel.setVisibility(View.GONE);

        // Currency text
        mStatusText = (TextView) view.findViewById(R.id.status_text);

        // Image
        mStatusImage = (ImageView) view.findViewById(R.id.status_image);

        // Progress
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.load_progress),
                tabs);

        // Load wallets
        LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
        loadWalletsTask.execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_home, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(getString(R.string.app_name));
        ((MainActivity)getActivity()).setBackButtonEnabled(false);

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

    public class LoadWalletsTask extends AsyncTaskHandleException<Void, Void, BlockchainParameter> {

        @Override
        protected void onPreExecute() {
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected BlockchainParameter doInBackgroundHandleException(Void... param) throws PeerConnectionException{

            DataContext dataContext = ServiceLocator.instance().getDataContext();
            Wallet currentWallet = dataContext.getWallet();
            BlockchainParameter result = dataContext.getBlockchainParameter();

            if (currentWallet == null || result == null) {
                ((Application)getActivity().getApplication()).getAccountId();

                // Set the peer to use for network
                io.ucoin.app.model.Peer node = new io.ucoin.app.model.Peer(
                        Configuration.instance().getNodeHost(),
                        Configuration.instance().getNodePort()
                );
                ServiceLocator.instance().getHttpService().connect(node);

                // Load currency
                result = ServiceLocator.instance().getBlockchainRemoteService().getParameters();
                dataContext.setBlockchainParameter(result);

                // Load default wallet
                Wallet defaultWallet = ServiceLocator.instance().getWalletService().getDefaultWallet(getActivity().getApplication());
                defaultWallet.setCurrency(result.getCurrency());
                dataContext.setWallet(defaultWallet);

                // Load the crypto service (load lib)
                ServiceLocator.instance().getCryptoService();
            }

            return result;
        }

        @Override
        protected void onSuccess(final BlockchainParameter result) {
            mProgressViewAdapter.showProgress(false);
            mStatusText.setText("");

            FragmentManager fm = getFragmentManager();
            if (fm.findFragmentByTag("tab1") == null) {
                fm.beginTransaction()
                        .replace(R.id.tab1, WalletListFragment.newInstance(), "tab1")
                        .commit();
            }
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
            mStatusPanel.setVisibility(View.VISIBLE);

            // Display the error
            Toast.makeText(HomeFragment.this.getActivity(),
                    errorMessage,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
