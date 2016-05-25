package org.duniter.app.view.currency;

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

import org.duniter.app.R;
import org.duniter.app.model.EntitySql.SourceSql;
import org.duniter.app.view.currency.adapter.SourceCursorAdapter;

public class SourceListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    static public SourceListFragment newInstance(Long currencyId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, currencyId);
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
        return inflater.inflate(R.layout.list_fragment_source,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.sources));
        setHasOptionsMenu(true);



        SourceCursorAdapter sourceCursorAdapter
                = new SourceCursorAdapter(getActivity(), null, 0);
        setListAdapter(sourceCursorAdapter);
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
        String selection = SourceSql.SourceTable.CURRENCY_ID + "=?";
        String[] selectionArgs = new String[]{currencyId.toString()};

        return new CursorLoader(
                getActivity(),
                SourceSql.URI,
                null, selection, selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SourceCursorAdapter) this.getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SourceCursorAdapter) this.getListAdapter()).swapCursor(null);
    }
}