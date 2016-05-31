package org.duniter.app.view.wallet;

import android.app.DialogFragment;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityServices.WalletService;
import org.duniter.app.model.EntitySql.view.ViewWalletAdapter;
import org.duniter.app.services.SqlService;
import org.duniter.app.view.MainActivity;
import org.duniter.app.view.TransferActivity;
import org.duniter.app.view.dialog.ConnectionDialogFragment;
import org.duniter.app.view.dialog.InscriptionDialogFragment;
import org.duniter.app.view.dialog.NewWalletDialogFragment;
import org.duniter.app.view.dialog.RecordingDialogFragment;
import org.duniter.app.view.wallet.adapter.WalletCursorAdapter;

public class WalletListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String NEW_WALLET = "new_wallet";

    private SwipeRefreshLayout mSwipeLayout;
    private WalletCursorAdapter walletCursorAdapter;
    private List<Wallet> wallets;

    private Currency currency;

    static public WalletListFragment newInstance(Currency currency,boolean newWallet) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Application.CURRENCY, currency);
        newInstanceArgs.putBoolean(NEW_WALLET,newWallet);
        WalletListFragment fragment = new WalletListFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(getActivity() instanceof MainActivity){
            ((MainActivity) getActivity()).setDrawerIndicatorEnabled(true);
        }

        return inflater.inflate(R.layout.list_fragment_wallet,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.wallet));
        setHasOptionsMenu(true);

        currency = (Currency) getArguments().getSerializable(Application.CURRENCY);

        walletCursorAdapter
                = new WalletCursorAdapter(getActivity(), null, 0);
        setListAdapter(walletCursorAdapter);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);

        wallets = SqlService.getWalletSql(getActivity()).getByCurrency(currency);

        //AsyncTask task = new ProgressTask(getActivity(),currency,wallets.get(0)).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(getArguments().getBoolean(NEW_WALLET)) {
            inflater.inflate(R.menu.toolbar_wallet_list, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_wallet:
                actionNew();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        long walletId = ((WalletCursorAdapter)l.getAdapter()).getIdWallet(position);
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).setCurrentFragment(
                    WalletFragment.newInstance(walletId)
            );
        }else if (getActivity() instanceof TransferActivity){
            ((TransferActivity)getActivity()).setWalletSelected(walletId);
            getActivity().onBackPressed();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection;
        String[] selectionArgs;

        selection = ViewWalletAdapter.CURRENCY_ID + "=?";
        selectionArgs = new String[]{String.valueOf(currency.getId())};

        return new CursorLoader(
                getActivity(),
                ViewWalletAdapter.URI,
                null, selection, selectionArgs,
                ViewWalletAdapter._ID + " ASC");

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((WalletCursorAdapter) this.getListAdapter()).swapCursor(data);
        for(Wallet wallet: wallets){
            WalletService.updateWallet(getActivity(),wallet,false,null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((WalletCursorAdapter) this.getListAdapter()).swapCursor(null);
    }

    private void actionNew() {
        DialogFragment whatNew = NewWalletDialogFragment.newInstance(new NewWalletDialogFragment.Action() {
            @Override
            public void actionNew() {
                actionInscriptionWallet();
            }

            @Override
            public void actionConnect() {
                actionConnectionWallet();
            }

            @Override
            public void actionRecording() {
                actionRecordingWallet();
            }
        });
        whatNew.show(getFragmentManager(), whatNew.getClass().getSimpleName());
    }

    private void actionInscriptionWallet() {
        InscriptionDialogFragment inscriptionDialogFragment = InscriptionDialogFragment.newInstance();
        inscriptionDialogFragment.show(getFragmentManager(), inscriptionDialogFragment.getClass().getSimpleName());
    }

    private void actionConnectionWallet() {
        ConnectionDialogFragment connectionDialogFragment = ConnectionDialogFragment.newInstance();
        connectionDialogFragment.show(getFragmentManager(), connectionDialogFragment.getClass().getSimpleName());
    }

    private void actionRecordingWallet() {
        RecordingDialogFragment recordingDialogFragment = RecordingDialogFragment.newInstance();
        recordingDialogFragment.show(getFragmentManager(), recordingDialogFragment.getClass().getSimpleName());
    }

    @Override
    public void onRefresh() {
        mSwipeLayout.setRefreshing(false);
        getLoaderManager().restartLoader(0, getArguments(), this);
    }

}


