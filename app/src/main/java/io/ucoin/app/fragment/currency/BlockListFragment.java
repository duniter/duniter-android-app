package io.ucoin.app.fragment.currency;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.adapter.BlockCursorAdapter;
import io.ucoin.app.sqlite.SQLiteTable;

public class BlockListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    static public BlockListFragment newInstance(Long currencyId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, currencyId);
        BlockListFragment fragment = new BlockListFragment();
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
        return inflater.inflate(R.layout.fragment_block_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.blocks));
        setHasOptionsMenu(true);

        BlockCursorAdapter blockCursorAdapter
                = new BlockCursorAdapter(getActivity(), null, 0);
        setListAdapter(blockCursorAdapter);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        /*UcoinCurrency currency = getArguments().getParcelable(UcoinCurrency.class.getSimpleName());
        UcoinBlock block = currency.blocks().getById(id);
*/
        //block.delete();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Long currencyId = args.getLong(BaseColumns._ID);
        String selection = SQLiteTable.Wallet.CURRENCY_ID + "=?";
        String[] selectionArgs = new String[]{currencyId.toString()};

        return new CursorLoader(
                getActivity(),
                UcoinUris.BLOCK_URI,
                null, selection, selectionArgs,
                SQLiteTable.Block.NUMBER + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((BlockCursorAdapter) this.getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((BlockCursorAdapter) this.getListAdapter()).swapCursor(null);
    }
}