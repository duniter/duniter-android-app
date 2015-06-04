package io.ucoin.app.fragment.wallet;

import android.support.v4.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.ucoin.app.R;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.adapter.MovementCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.database.Contract;
import io.ucoin.app.database.Provider;
import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.local.UnitType;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.service.ServiceLocator;


public class MovementListFragment extends android.support.v4.app.ListFragment {

    protected static final String BUNDLE_WALLET_ID = "WalletId";

    private MovementCursorAdapter mCursorAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private MovementListListener mListener;
    private String mUnitType;

    public static MovementListFragment newInstance(Wallet wallet, MovementListListener listener) {
        MovementListFragment fragment = new MovementListFragment();
        Bundle args = new Bundle();
        args.putLong(BUNDLE_WALLET_ID, wallet.getId());
        fragment.setArguments(args);

        fragment.setOnClickListener(listener);

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

        // Read the default unit to use
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        mUnitType = preferences.getString(SettingsActivity.PREF_UNIT, UnitType.COIN);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                getListView());


        mCursorAdapter = new MovementCursorAdapter((Context) getActivity(), getNewCursor(), 0, mUnitType);
        setListAdapter(mCursorAdapter);

        //
        //onRefreshMovements();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_movement_list, menu);
    }


    public void notifyDataSetChanged() {
        // Update the adapter's cursor
        mCursorAdapter.swapCursor(getNewCursor());

        // Send notification to the list view
        mCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener == null) {
            return;
        }

        Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
        Movement movement = ServiceLocator.instance().getMovementService().toMovement(cursor);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Movement.class.getSimpleName(), movement);
        mListener.onPositiveClick(bundle);
    }

    /* -- Inner class -- */

    public interface MovementListListener {
        public void onPositiveClick(Bundle args);
    }

    /* -- Internal methods -- */

    private void setOnClickListener(MovementListListener listener) {
        mListener = listener;
    }

    protected Cursor getNewCursor() {
        long walletId = getArguments().getLong(BUNDLE_WALLET_ID);

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/movement/");

        String selection;
        String[] selectionArgs;

        // If unit is mutal credit: do not display UD
        if (SettingsActivity.PREF_UNIT_TIME.equals(mUnitType)) {
            selection = Contract.Movement.WALLET_ID + "=? AND IS_UD=?";
            selectionArgs = new String[] {String.valueOf(walletId), "0"};
        }
        else {
            selection = Contract.Movement.WALLET_ID + "=?";
            selectionArgs = new String[] {String.valueOf(walletId)};
        }

        String orderBy = Contract.Movement.TIME + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, orderBy);

        return cursor;
    }
}
