package io.ucoin.app.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.model.Wallet;


public class WalletListFragment extends ListFragment implements MainActivity.QueryResultListener<Wallet>{

    private static final String WALLET_LIST_ARGS_KEYS = "Wallets";

    private WalletArrayAdapter mWalletArrayAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private OnClickListener mListener;

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

        // load progress
        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.search_progress),
                getListView());
        // Display the progress by default (onQuerySuccess will disable it)
        mProgressViewAdapter.showProgress(true);

        TextView v = (TextView) view.findViewById(android.R.id.empty);
        v.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                // TODO : show a "new wallet fragment"
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

    private void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }

    @Override
    public void onQuerySuccess(List<? extends Wallet> wallets) {
        mWalletArrayAdapter.clear();
        mWalletArrayAdapter.addAll(wallets);
        mWalletArrayAdapter.notifyDataSetChanged();
        mProgressViewAdapter.showProgress(false);
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

}
