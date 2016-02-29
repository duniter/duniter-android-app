package io.ucoin.app.fragment.currency;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.adapter.PeerCursorTreeAdapter;
import io.ucoin.app.fragment.dialog.AddPeerDialogFragment;
import io.ucoin.app.model.sql.sqlite.Peer;
import io.ucoin.app.sqlite.SQLiteTable;

public class PeerListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        DialogInterface.OnDismissListener,
        ImageButton.OnClickListener,
        ExpandableListView.OnGroupClickListener {

    private final int PEER_LOADER_ID = -1;

    private ImageButton mButton;
    private ExpandableListView mListView;

    static public PeerListFragment newInstance(Long currencyId) {
        Bundle newInstanceargs = new Bundle();
        newInstanceargs.putLong(BaseColumns._ID, currencyId);
        PeerListFragment fragment = new PeerListFragment();
        fragment.setArguments(newInstanceargs);
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
        return inflater.inflate(R.layout.fragment_peer_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.peers));
        setHasOptionsMenu(true);

        ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(true);

        mButton = (ImageButton) view.findViewById(R.id.add_peer_button);
        if(!BuildConfig.DEBUG) {
            mButton.setVisibility(View.GONE);
        }
        PeerCursorTreeAdapter peerCursorTreeAdapter
                = new PeerCursorTreeAdapter(null, getActivity());
        mListView = (ExpandableListView) view.findViewById(R.id.list);
        mListView.setAdapter(peerCursorTreeAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty));
        mListView.setOnGroupClickListener(this);
        getLoaderManager().initLoader(PEER_LOADER_ID, getArguments(), this);
        if(BuildConfig.DEBUG) {
            registerForContextMenu(mListView);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, getResources().getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Long peerId = acmi.id;
        Peer peer = new Peer(getActivity(), peerId);
        peer.delete();
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == PEER_LOADER_ID) {
            Long currencyId = args.getLong(BaseColumns._ID);
            String selection = null;
            String[] selectionArgs = null;
            if(!currencyId.equals(Long.valueOf(-1))){
                selection = SQLiteTable.Peer.CURRENCY_ID + "=?";
                selectionArgs = new String[]{currencyId.toString()};
            }
            return new CursorLoader(
                    getActivity(),
                    UcoinUris.PEER_URI,
                    null, selection, selectionArgs,
                    BaseColumns._ID + " ASC");
        } else {
            Long peerId = args.getLong(BaseColumns._ID);
            String selection = SQLiteTable.Endpoint.PEER_ID + "=?";
            String selectionArgs[] = new String[]{peerId.toString()};
            return new CursorLoader(
                    getActivity(),
                    UcoinUris.ENDPOINT_URI,
                    null, selection, selectionArgs,
                    BaseColumns._ID + " ASC ");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == PEER_LOADER_ID) {
            ((PeerCursorTreeAdapter) mListView.getExpandableListAdapter()).setGroupCursor(data);
        } else {
            ((PeerCursorTreeAdapter) mListView.getExpandableListAdapter()).setChildrenCursor(loader.getId(), data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == PEER_LOADER_ID) {
            ((PeerCursorTreeAdapter) mListView.getExpandableListAdapter()).setGroupCursor(null);
        } else {
            ((PeerCursorTreeAdapter) mListView.getExpandableListAdapter()).setChildrenCursor(loader.getId(), null);
        }
    }


    @Override
    public void onClick(View v) {
        RotateAnimation animation = new RotateAnimation(0, 145, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        mButton.startAnimation(animation);

        AddPeerDialogFragment fragment =
                AddPeerDialogFragment.newInstance(getArguments().getLong(BaseColumns._ID));
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), fragment.getClass().getSimpleName());
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (!parent.isGroupExpanded(groupPosition)) {
            Bundle args = new Bundle();
            args.putLong(BaseColumns._ID, id);
            getLoaderManager().initLoader(groupPosition, args, this);
        } else {
            getLoaderManager().destroyLoader(groupPosition);
        }
        return false;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        RotateAnimation animation = new RotateAnimation(145, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        mButton.startAnimation(animation);
    }
}