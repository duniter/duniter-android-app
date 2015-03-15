package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.ExceptionUtils;


public class WalletListFragment extends ListFragment implements MainActivity.QueryResultListener<Wallet>{

    private static final String TAG = "WalletListFragment";
    private static final String WALLET_LIST_ARGS_KEYS = "Wallets";

    private WalletArrayAdapter mWalletArrayAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private ProgressBar mUpdateCreditProgressBar;
    private OnClickListener mListener;
    private int mScrollState;
    private ListView mListView;
    private UpdaterAsyncTask mUpdaterTask;

    protected static WalletListFragment newInstance(OnClickListener listener) {
        WalletListFragment fragment = new WalletListFragment();
        fragment.setOnClickListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // list adapter
        mWalletArrayAdapter = new WalletArrayAdapter(getActivity());
        setListAdapter(mWalletArrayAdapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_wallet_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = getListView();

        // search progress
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.search_progress),
                getListView());

        // refresh progress
        mUpdateCreditProgressBar = (ProgressBar)view.findViewById(R.id.load_progress);

        // Display the progress by default (onQuerySuccess will disable it)
        mProgressViewAdapter.showProgress(true);
        TextView v = (TextView) view.findViewById(android.R.id.empty);
        v.setVisibility(View.GONE);

        // add button
        /*ImageButton addButton = (ImageButton) view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddWalletClick();
            }
        });*/

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_wallet:
                onAddWalletClick();
                return true;
            case R.id.action_refresh:
                onRefreshClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener == null) {
            return;
        }
        Wallet wallet = (Wallet) l.getAdapter().getItem(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Wallet.class.getSimpleName(), wallet);
        mListener.onPositiveClick(bundle);
    }


    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }

    @Override
    public void onQuerySuccess(List<? extends Wallet> wallets) {
        mWalletArrayAdapter.setNotifyOnChange(true);
        mWalletArrayAdapter.clear();
        mWalletArrayAdapter.addAll(wallets);
        mWalletArrayAdapter.notifyDataSetChanged();
        mProgressViewAdapter.showProgress(false);

        // Run the updater task
        if (CollectionUtils.isNotEmpty(wallets)) {
            boolean refreshNeed = true;
            // TODO : manage a time delay before refresh ?

            if (refreshNeed) {
                UpdaterAsyncTask updaterTask = new UpdaterAsyncTask(mUpdateCreditProgressBar);
                updaterTask.execute(wallets.toArray(new Wallet[wallets.size()]));
            }
        }
    }

    @Override
    public void onQueryFailed(String message) {
        mProgressViewAdapter.showProgress(false);
        // TODO display the message
    }

    @Override
    public void onQueryCancelled() {
        mProgressViewAdapter.showProgress(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Stop the updater task
        if (mUpdaterTask != null) {
            mUpdaterTask.stop();
            mUpdaterTask = null;
        }
    }

    /* -- Internal methods -- */

    private void onRefreshClick(){
        int count = mWalletArrayAdapter.getCount();
        if (count == 0) {
            return;
        }

        // Copy wallet to array, from adapter
        Wallet[] wallets = new Wallet[count];
        for (int i = 0; i<count; i++) {
            wallets[i] = mWalletArrayAdapter.getItem(i);
        }

        // run the refresh task
        UpdaterAsyncTask updaterTask = new UpdaterAsyncTask(mUpdateCreditProgressBar);
        updaterTask.execute(wallets);
    }

    private void setOnClickListener(OnClickListener listener) {
         mListener = listener;
     }

    protected void onAddWalletClick() {
        Fragment fragment = AddWalletFragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        // Insert the Home at the first place in back stack
        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.slide_in_right,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_left)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private class UpdaterAsyncTask extends AsyncTaskHandleException<Wallet, Void, Void> {

        boolean isRunning = true;
        private Activity mActivity = getActivity();

        public UpdaterAsyncTask(ProgressBar progressBar) {
            super(progressBar, null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mUpdateCreditProgressBar.setVisibility(View.VISIBLE);
        }

        public void stop() {
            isRunning = false;
        }

        @Override
        protected Void doInBackgroundHandleException(final Wallet... wallets) {
            int i=0;
            int count = wallets.length;

            setMax(count);
            setProgress(0);

            while (isRunning && i < count) {
                Wallet wallet = wallets[i++];

                ServiceLocator.instance().getWalletService().updateWallet(mActivity, wallet);

                // Gather data about your adapter objects
                // If an object has changed, mark it as dirty

                increment();
            }

            return null;
        }

        /*@Override
        protected void onProgressUpdate(final Void... values) {
            super.onProgressUpdate(values);

            // Update only when we're not scrolling, and only for visible views
            if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                int start = mListView.getFirstVisiblePosition();
                for(int i = start, j = mListView.getLastVisiblePosition(); i<=j; i++) {
                    View view = mListView.getChildAt(i-start);
                    Wallet wallet = (Wallet)mListView.getItemAtPosition(i);
                    if (wallet.isDirty()) {
                        mListView.getAdapter().getView(i, view, mListView); // Tell the adapter to update this view
                        wallet.setDirty(false);
                    }

                }
            }

        }*/

        @Override
        protected void onSuccess(Void aVoid) {
            mUpdateCreditProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onFailed(Throwable t) {
            mUpdateCreditProgressBar.setVisibility(View.GONE);
            Log.d(TAG, "Error in updated task", t);
            Toast.makeText(mActivity,
                    ExceptionUtils.getMessage(t),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}
