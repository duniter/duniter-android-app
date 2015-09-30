package io.ucoin.app.fragment.contact;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.adapter.ContactCursorRecyclerViewAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.view.DividerItemDecoration;


public class ContactListFragment extends Fragment {

    public static final String BUNDLE_CONTACT_ID = "ContactId";

    private SearchView mSearchEditText;
    private RecyclerView mRecyclerView;
    private ContactCursorRecyclerViewAdapter mContactAdapter;
    private LinearLayoutManager mLayoutManager;
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

        Cursor cursor = createCursor(null);

        mSearchEditText = (SearchView) view.findViewById(R.id.search_text);
        mSearchEditText.onActionViewExpanded();
        mSearchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s)) {
                    mSearchEditText.clearFocus();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Cursor cursor = createCursor(query.toString());
                mContactAdapter.swapCursor(cursor);
                return false;
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.contact_recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mContactAdapter = new ContactCursorRecyclerViewAdapter(getActivity().getApplicationContext(),
                cursor, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    int position = mRecyclerView.getChildPosition(view);
                    Long contactId = mContactAdapter.getItemId(position);
                    Bundle bundle = new Bundle();
                    bundle.putLong(BUNDLE_CONTACT_ID, contactId);
                    mListener.onPositiveClick(bundle);
                }
            }
        });
        mRecyclerView.setAdapter(mContactAdapter);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.contact_progressbar),
                mRecyclerView);
    }

    /* -- Inner class -- */

    public interface ContactListListener {
        public void onPositiveClick(Bundle args);
    }

    /* -- Internal methods -- */

    private void setOnClickListener(ContactListListener listener) {
        mListener = listener;
    }

    private Cursor createCursor(String nameFilter) {
        StringBuilder selection = new StringBuilder();
        String[] selectionArgs;
        if (StringUtils.isNotBlank(nameFilter)) {
            selection.append(SQLiteTable.Contact.ACCOUNT_ID).append("=?")
                    .append(" AND UPPER(").append(SQLiteTable.Contact.NAME).append(") like UPPER(? || '%')");
            selectionArgs = new String[]{
                    ((Application) getActivity().getApplication()).getAccountIdAsString(),
                    nameFilter
            };
        }
        else {
            selection.append(SQLiteTable.Contact.ACCOUNT_ID).append("=?");
            selectionArgs = new String[]{
                    ((Application) getActivity().getApplication()).getAccountIdAsString()
            };
        }

        String orderBy = "LOWER(" + SQLiteTable.Contact.NAME + ") ASC";

        Cursor cursor = getActivity().getContentResolver().query(Provider.CONTACT_VIEW_URI,
                new String[]{},
                selection.toString(),
                selectionArgs,
                orderBy);

        return cursor;
    }
}
