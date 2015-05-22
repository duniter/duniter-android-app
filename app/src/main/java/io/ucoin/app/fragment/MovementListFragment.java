package io.ucoin.app.fragment;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.adapter.MovementCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.task.NullAsyncTaskListener;


public class MovementListFragment extends ListFragment {

    protected static final String BUNDLE_WALLET_ID = "WalletId";

    private MovementCursorAdapter mCursorAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    public static MovementListFragment newInstance(Wallet wallet) {
        MovementListFragment fragment = new MovementListFragment();
        Bundle args = new Bundle();
        args.putLong(BUNDLE_WALLET_ID, wallet.getId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_movement_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                getListView());
        long walletId = getArguments().getLong(BUNDLE_WALLET_ID);

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/movement/");

        String selection = Contract.Movement.WALLET_ID + "=?";
        String[] selectionArgs = {String.valueOf(walletId)};
        String orderBy = Contract.Movement.TIME + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, orderBy);
        mCursorAdapter = new MovementCursorAdapter((Context) getActivity(), cursor, 0);
        setListAdapter(mCursorAdapter);

        //
        //onRefreshMovements();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_movement_list, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                onRefreshMovements();
                return true;
            case R.id.action_resync:
                onRefreshAllMovements();
                return true;
        }

        return false;
    }

    protected void onRefreshAllMovements() {

        // Launch after user confirmation
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.sync))
                .setMessage(getString(R.string.resync_confirm))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Launch
                        doOnRefreshMovements(true);
                    }})
                .setNegativeButton(android.R.string.no, null).show();

    }

    protected void onRefreshMovements() {
        doOnRefreshMovements(false);
    }

    protected void doOnRefreshMovements(final boolean doCompleteRefresh) {
        long accountId = ((Application)getActivity().getApplication()).getAccountId();
        long walletId = getArguments().getLong(BUNDLE_WALLET_ID);

        final long time1 = System.currentTimeMillis();
        ServiceLocator serviceLocator = ServiceLocator.instance();

        // Refresh movements
        serviceLocator.getMovementService().refreshMovements(
                accountId,
                walletId,
                doCompleteRefresh,
                new NullAsyncTaskListener<Long>(getActivity().getApplicationContext()) {
                    @Override
                    public void onSuccess(final Long nbUpdates) {
                        if (nbUpdates != null && nbUpdates.longValue() > 0) {
                            long duration = System.currentTimeMillis() - time1;
                            onFinishRefresh(nbUpdates.longValue(), duration);
                        }
                    }
                });

        Toast.makeText(getActivity(), getString(R.string.resync_started), Toast.LENGTH_SHORT).show();
    }

    protected void onFinishRefresh(long nbUpdates, long timeInMillis) {

        mCursorAdapter.notifyDataSetChanged();

        String message = getString(R.string.sync_succeed,
                nbUpdates,
                DateUtils.formatFriendlyDateTime(getActivity(), timeInMillis / 1000));
        Toast.makeText(getActivity(),
                message
                , Toast.LENGTH_LONG).show();
    }
}
