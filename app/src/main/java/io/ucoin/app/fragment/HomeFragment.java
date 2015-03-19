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

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.ViewUtils;


public class HomeFragment extends Fragment {

    private View mStatusPanel;
    private TextView mStatusText;
    private ImageView mStatusImage;
    private TabHost mTabs;
    private MainActivity.QueryResultListener<Wallet> mWalletResultListener;

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
        mTabs = (TabHost)view.findViewById(R.id.tabHost);
        mTabs.setup();
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator(getString(R.string.wallets));
            mTabs.addTab(spec);
        }
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator(getString(R.string.favorites));
            mTabs.addTab(spec);
        }

        mStatusPanel = view.findViewById(R.id.status_panel);
        mStatusPanel.setVisibility(View.GONE);

        // Currency text
        mStatusText = (TextView) view.findViewById(R.id.status_text);

        // Image
        mStatusImage = (ImageView) view.findViewById(R.id.status_image);

        // Tab 1: wallet list
        {
            WalletListFragment fragment1 = WalletListFragment.newInstance(
                    // Manage click on wallet
                    new WalletListFragment.OnClickListener() {
                        @Override
                        public void onPositiveClick(Bundle args) {
                            Wallet wallet = (Wallet) args.getSerializable(Wallet.class.getSimpleName());
                            onWalletClick(wallet);
                        }
                    });
            mWalletResultListener = fragment1;
            getFragmentManager().beginTransaction()
                    .replace(R.id.tab1, fragment1, "tab1")
                    .commit();
        }

        // Tab 2: contact list
        {
            ContactListFragment fragment2 = ContactListFragment.newInstance();
            getFragmentManager().beginTransaction()
                .replace(R.id.tab2, fragment2, "tab2")
                .commit();
        }

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

    protected void onWalletClick(Wallet wallet) {
        Fragment fragment = WalletFragment.newInstance(wallet);
        FragmentManager fragmentManager = getFragmentManager();
        // Insert the Home at the first place in back stack
        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public class LoadWalletsTask extends AsyncTaskHandleException<Void, Void, List<Wallet>> {

        @Override
        protected List<Wallet> doInBackgroundHandleException(Void... param) throws PeerConnectionException{
            // Load currencies cache
            ServiceLocator.instance().getCurrencyService().loadCache(getActivity().getApplication());

            // Load peers cache
            ServiceLocator.instance().getPeerService().loadCache(getActivity().getApplication());

            // Load wallets
            return ServiceLocator.instance().getWalletService()
                    .getWallets(getActivity().getApplication());
        }

        @Override
        protected void onSuccess(final List<Wallet> wallets) {
            mWalletResultListener.onQuerySuccess(wallets);
        }

        @Override
        protected void onFailed(Throwable t) {
            final String errorMessage = getString(R.string.connected_error, t.getMessage());
            Log.e(getClass().getSimpleName(), errorMessage, t);

            // TODO send a message ?
            mWalletResultListener.onQueryFailed(null);

            // TODO: should never append here (no network connection)
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
            ViewUtils.toogleViews(mTabs, mStatusPanel);

            // Display the error
            Toast.makeText(HomeFragment.this.getActivity(),
                    errorMessage,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
