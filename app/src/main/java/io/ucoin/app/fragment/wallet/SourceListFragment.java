package io.ucoin.app.fragment.wallet;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.adapter.SourceCursorAdapter;
import io.ucoin.app.sqlite.SQLiteTable;


public class SourceListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>    {

    static public SourceListFragment newInstance(Long walletId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, walletId);
        SourceListFragment fragment = new SourceListFragment();
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
        return inflater.inflate(R.layout.fragment_source_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SourceCursorAdapter sourceCursorAdapter
                = new SourceCursorAdapter(getActivity(), null, 0);
        setListAdapter(sourceCursorAdapter);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Long walletId = args.getLong(BaseColumns._ID);
        String selection = SQLiteTable.Source.WALLET_ID + "=?";
        String selectionArgs[] = new String[]{
                walletId.toString()
        };

        return new CursorLoader(
                getActivity(),
                UcoinUris.SOURCE_URI,
                null, selection, selectionArgs,
                SQLiteTable.Source.NUMBER +" DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SourceCursorAdapter)this.getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SourceCursorAdapter)this.getListAdapter()).swapCursor(null);
    }
}
