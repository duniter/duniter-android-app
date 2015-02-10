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

import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.WalletArrayAdapter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;


public class WalletListFragment extends ListFragment {

    private static final String WALLET_LIST_ARGS_KEYS = "Wallets";

    private WalletArrayAdapter mWalletArrayAdapter;
    private OnClickListener mListener;

    public static WalletListFragment newInstance(OnClickListener listener) {
        return newInstance(listener, new ArrayList<Wallet>());
    }

    protected static WalletListFragment newInstance(OnClickListener listener, List<Wallet> wallets) {
        WalletListFragment fragment = new WalletListFragment();
        fragment.setOnClickListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // list adapter
        List<Wallet> wallets = ServiceLocator.instance().getDataContext().getWallets();
        if(wallets != null)
        {
            mWalletArrayAdapter = new WalletArrayAdapter(getActivity(), wallets);
        }
        else {
            mWalletArrayAdapter = new WalletArrayAdapter(getActivity());
        }
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

}
