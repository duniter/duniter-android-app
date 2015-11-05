package io.ucoin.app.fragment.wallet;

import android.app.Fragment;
import android.database.Cursor;
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

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.MovementCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.technical.view.DividerItemDecoration;


public class MovementListFragment<T> extends Fragment {

    public static final String BUNDLE_ID = "Id";
    public static final String BUNDLE_MOVEMENT_ID = "MovementId";
    public static final String BUNDLE_MOVEMENT_PUBKEY = "pubkey";
    public static final String BUNDLE_MOVEMENT_CURRENCY_ID = "currencyId";
    public static final int LISTENER_MOUVEMENT_CLICK = 1;
    public static final int LISTENER_PUBKEY_CLICK = 2;

    public static List<Wallet> wallets;

    private MovementCursorAdapter mRecyclerViewAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private MovementListListener mListener;
    private RecyclerView mRecyclerView;
    private int type;
    private LinearLayoutManager mLayoutManager;
    private static Wallet _wallet;
    private static Identity _identity;

    public static MovementListFragment newInstance(Wallet wallet, MovementListListener listener) {
        MovementListFragment fragment = new MovementListFragment();
        Bundle args = new Bundle();
        args.putSerializable(MouvementFragment.TYPE, MouvementFragment.WALLET);
//        args.putSerializable(Wallet.class.getSimpleName(), wallet);
        args.putLong(BUNDLE_ID, wallet.getId());
        fragment.setArguments(args);

        fragment.setOnClickListener(listener);
        wallets = null;

        _wallet = wallet;


        return fragment;
    }
    public static MovementListFragment newInstance(Identity identity,List<Wallet> ws, MovementListListener listener) {
        MovementListFragment fragment = new MovementListFragment();
        Bundle args = new Bundle();
        args.putSerializable(MouvementFragment.TYPE, MouvementFragment.IDENTITY);
//        args.putSerializable(Wallet.class.getSimpleName(), wallet);
        args.putString(BUNDLE_MOVEMENT_PUBKEY, identity.getPubkey());
        fragment.setArguments(args);

        fragment.setOnClickListener(listener);
        wallets = ws;

        _identity = identity;


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

        Bundle newInstanceArgs = getArguments();

        type = (int) newInstanceArgs.getSerializable(MouvementFragment.TYPE);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if(type == MouvementFragment.WALLET) {
            mRecyclerViewAdapter = new MovementCursorAdapter<Wallet>(getActivity().getApplicationContext(), _wallet, wallets, getNewCursor(), new MovementCursorAdapter.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = mRecyclerView.getChildPosition(view);
                        Long movementId = mRecyclerViewAdapter.getItemId(position);
                        Bundle bundle = new Bundle();
                        bundle.putLong(BUNDLE_MOVEMENT_ID, movementId);
                        mListener.onPositiveClick(bundle, LISTENER_MOUVEMENT_CLICK);
                    }
                }

                @Override
                public void onClick(Bundle args) {
                    if (mListener != null) {
                        mListener.onPositiveClick(args, LISTENER_PUBKEY_CLICK);
                    }
                }
            });
        }else if (type == MouvementFragment.IDENTITY){
            mRecyclerViewAdapter = new MovementCursorAdapter<Identity>(getActivity().getApplicationContext(), _identity, wallets, getNewCursor(), new MovementCursorAdapter.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        int position = mRecyclerView.getChildPosition(view);
                        Long movementId = mRecyclerViewAdapter.getItemId(position);
                        Bundle bundle = new Bundle();
                        bundle.putLong(BUNDLE_MOVEMENT_ID, movementId);
                        mListener.onPositiveClick(bundle, LISTENER_MOUVEMENT_CLICK);
                    }
                }

                @Override
                public void onClick(Bundle args) {
                    if (mListener != null) {
                        mListener.onPositiveClick(args, LISTENER_PUBKEY_CLICK);
                    }
                }
            });
        }


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
        public void onPositiveClick(Bundle args,int i);
    }

    /* -- Internal methods -- */

    private void setOnClickListener(MovementListListener listener) {
        mListener = listener;
    }

    protected Cursor getNewCursor() {
        long valueId = getArguments().getLong(BUNDLE_ID);
        String selection = "";
        String[] selectionArgs = null;
        String orderBy = SQLiteTable.Movement.TIME + " DESC";

        if( type == MouvementFragment.WALLET) {
            selection = SQLiteTable.Movement.WALLET_ID + "=?";
            selectionArgs = new String[]{String.valueOf(valueId)};

        }else if (type == MouvementFragment.IDENTITY){
            String pubkey =getArguments().getString(BUNDLE_MOVEMENT_PUBKEY);
            //TODO FMA filter by Account
            selection = SQLiteTable.Movement.RECEIVERS + "=?" +
            " OR " + SQLiteTable.Movement.ISSUERS + "=?";
            selectionArgs = new String[]{pubkey,pubkey};
        }

        Cursor cursor = getActivity().getContentResolver().query(
                Provider.MOVEMENT_URI,
                new String[]{},
                selection,
                selectionArgs,
                orderBy);

        return cursor;
    }
}
