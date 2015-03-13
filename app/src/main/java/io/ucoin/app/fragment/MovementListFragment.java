package io.ucoin.app.fragment;

import android.app.ListFragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.MovementCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Wallet;


public class MovementListFragment extends ListFragment {

    protected static final String BUNDLE_WALLET_ID = "WalletId";

    private MovementCursorAdapter mCursorAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    static public MovementListFragment newInstance(Wallet wallet) {
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_movement_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.transactions);
        ((MainActivity) getActivity()).setBackButtonEnabled(true);
    }
}
