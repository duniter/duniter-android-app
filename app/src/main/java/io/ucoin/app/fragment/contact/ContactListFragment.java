package io.ucoin.app.fragment.contact;

import android.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ContactCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.content.Provider;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.service.ServiceLocator;


public class ContactListFragment extends ListFragment {

    protected static final String BUNDLE_WALLET_ID = "WalletId";

    private ContactCursorAdapter mCursorAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private ContactListListener mListener;

    static public ContactListFragment newInstance(ContactListListener listener) {
        ContactListFragment fragment = new ContactListFragment();
        fragment.setOnClickListener(listener);
        Bundle args = new Bundle();
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
        return inflater.inflate(R.layout.fragment_contact_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                getListView());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/contactView/");

        String selection = SQLiteTable.Contact.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                ((Application) getActivity().getApplication()).getAccountIdAsString()
        };
        String orderBy = SQLiteTable.Contact.NAME + " ASC";

        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, orderBy);
        mCursorAdapter = new ContactCursorAdapter((Context) getActivity(), cursor, 0);
        setListAdapter(mCursorAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_contact_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return ((MainActivity) getActivity()).onQueryTextSubmit(searchItem, s);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchView.setIconified(true);
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mListener == null) {
            return;
        }

        Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
        Contact contact = ServiceLocator.instance().getContactService().toContactFromView(cursor);

        Bundle bundle = new Bundle();
        bundle.putSerializable(Contact.class.getSimpleName(), contact);
        mListener.onPositiveClick(bundle);
    }

    /* -- Inner class -- */

    public interface ContactListListener {
        public void onPositiveClick(Bundle args);
    }

    /* -- Internal methods -- */

    private void setOnClickListener(ContactListListener listener) {
        mListener = listener;
    }


}
