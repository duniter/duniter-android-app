package io.ucoin.app.fragment.wallet;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.ucoin.app.R;
import io.ucoin.app.adapter.MovementCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.content.Provider;
import io.ucoin.app.model.local.Movement;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.view.DividerItemDecoration;


public class MovementListFragment extends Fragment {

    public static final String BUNDLE_WALLET_ID = "WalletId";
    public static final String BUNDLE_MOVEMENT_ID = "MovementId";

    private MovementCursorAdapter mRecyclerViewAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private MovementListListener mListener;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

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

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerViewAdapter = new MovementCursorAdapter(getActivity().getApplicationContext(),
                getNewCursor(),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mListener != null) {
                            int position = mRecyclerView.getChildPosition(view);
                            Long movementId = mRecyclerViewAdapter.getItemId(position);
                            Bundle bundle = new Bundle();
                            bundle.putLong(BUNDLE_MOVEMENT_ID, movementId);
                            mListener.onPositiveClick(bundle);
                        }
                    }
                });
        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                mRecyclerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_movement_list, menu);
    }

    public void notifyDataSetChanged() {
        // Update the adapter's cursor
        mRecyclerViewAdapter.swapCursor(getNewCursor());

        // Send notification to the list view
        mRecyclerViewAdapter.notifyDataSetChanged();
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

        String selection = SQLiteTable.Movement.WALLET_ID + "=?";
        String[] selectionArgs = {String.valueOf(walletId)};
        String orderBy = SQLiteTable.Movement.TIME + " DESC";

        Cursor cursor = getActivity().getContentResolver().query(
                Provider.MOVEMENT_URI,
                new String[]{},
                selection,
                selectionArgs,
                orderBy);

        return cursor;
    }
}
