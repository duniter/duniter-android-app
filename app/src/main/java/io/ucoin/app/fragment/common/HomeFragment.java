package io.ucoin.app.fragment.common;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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

import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.WalletRecyclerAdapter;
import io.ucoin.app.fragment.wallet.WalletFragment;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.ProgressDialogAsyncTaskListener;


public class HomeFragment extends Fragment {

    private TextView mUpdateDateLabel;
    private View mStatusPanel;
    private TextView mStatusText;
    private ImageView mStatusImage;
    private WalletRecyclerAdapter mWalletRecyclerAdapter;
    private RecyclerView mRecyclerView;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mWalletRecyclerAdapter = new WalletRecyclerAdapter(getActivity(), null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = mRecyclerView.getChildPosition(view);
                onWalletClick(mWalletRecyclerAdapter.getItem(position));
            }
        });
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

        // TODO update this label
        mUpdateDateLabel = (TextView)view.findViewById(R.id.update_date_label);

        // Status
        {
            mStatusPanel = view.findViewById(R.id.status_panel);
            mStatusPanel.setVisibility(View.GONE);

            // Currency text
            mStatusText = (TextView) view.findViewById(R.id.status_text);

            // Image
            mStatusImage = (ImageView) view.findViewById(R.id.status_image);
        }

        // Recycler view
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mWalletRecyclerAdapter);

        // If no result
        TextView v = (TextView) view.findViewById(android.R.id.empty);
        v.setVisibility(View.GONE);

        // Load wallets
        LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
        loadWalletsTask.execute();

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_dashboard, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle("");
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(false);
        }

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
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

    protected void onWalletClick(final Wallet wallet) {
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

    protected void onWalletListLoadFailed(Throwable t) {

        final String errorMessage = getString(R.string.connected_error, t.getMessage());
        Log.e(getClass().getSimpleName(), errorMessage, t);

        // Display the error
        Toast.makeText(getActivity(),
                errorMessage,
                Toast.LENGTH_SHORT)
                .show();

        // Error when no network connection
        mStatusText.setText(getString(R.string.not_connected));
        mStatusImage.setImageResource(R.drawable.warning45);
        mStatusImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display the error on click
                Toast.makeText(getActivity(),
                        getString(R.string.connected_error, errorMessage),
                        Toast.LENGTH_LONG)
                        .show();
            }
        });

        ViewUtils.toogleViews(mUpdateDateLabel, mStatusPanel);

        // Display the error
        Toast.makeText(getActivity(),
                errorMessage,
                Toast.LENGTH_SHORT)
                .show();
    }


    public class LoadWalletsTask extends AsyncTaskHandleException<Void, Void, List<Wallet>> {

        private final long mAccountId;

        public LoadWalletsTask() {
            super(getActivity().getApplicationContext());
            mAccountId = ((Application)getActivity().getApplication()).getAccountId();

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            ProgressDialogAsyncTaskListener listener = new ProgressDialogAsyncTaskListener(progressDialog);
            setListener(listener);
        }

        @Override
        protected List<Wallet> doInBackgroundHandleException(Void... param) throws PeerConnectionException {
            ServiceLocator serviceLocator = ServiceLocator.instance();

            setMax(100);
            setProgress(0);

            // Load wallets
            return serviceLocator.getWalletService().getWalletsByAccountId(
                    getContext(),
                    mAccountId,
                    true,
                    LoadWalletsTask.this);
        }

        @Override
        protected void onSuccess(final List<Wallet> wallets) {
            mWalletRecyclerAdapter.clear();
            mWalletRecyclerAdapter.addAll(wallets);
            mWalletRecyclerAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onFailed(Throwable t) {
            onWalletListLoadFailed(t);
        }
    }
}
