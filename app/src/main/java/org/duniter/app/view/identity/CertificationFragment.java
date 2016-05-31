package org.duniter.app.view.identity;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.enumeration.CertificationType;
import org.duniter.app.model.Entity.Certification;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntitySql.view.ViewCertificationAdapter;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackCertify;
import org.duniter.app.technical.callback.CallbackIdentity;
import org.duniter.app.view.MainActivity;
import org.duniter.app.view.identity.adapter.CertificationBaseAdapter;
import org.duniter.app.view.identity.adapter.CertificationCursorAdapter;
import org.duniter.app.view.dialog.ListIdentityDialogFragment;

public class CertificationFragment extends ListFragment
        implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener{

    private ProgressBar                progress;
    private SwipeRefreshLayout mSwipeLayout;
    private String                     publicKey;
    private Long                       identityId;
    private Long                       currencyId;
    private Currency currency;
    private Contact contact;
    private CertificationBaseAdapter   certificationBaseAdapter;
    private CertificationCursorAdapter certificationCursorAdapter;
    private Identity identitySelected;
    private Cursor mCursor;

    static public CertificationFragment newInstance(Bundle args) {
        CertificationFragment fragment = new CertificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(
            @NonNull
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
        }
        if (savedInstanceState != null) {
            getArguments().putSerializable(Application.CONTACT,
                                           savedInstanceState.getSerializable(Application.CONTACT));
        }
        return inflater.inflate(R.layout.list_fragment_certification, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Application.CONTACT,
                                 getArguments().getSerializable(Application.CONTACT));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);
        TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeLayout.setRefreshing(true);
                onRefresh();
            }
        });
        progress = (ProgressBar) view.findViewById(R.id.progress_bar);
        publicKey = getArguments().getString(Application.PUBLIC_KEY);
        contact = (Contact) getArguments().getSerializable(Application.CONTACT);
        currencyId = getArguments().getLong(Application.CURRENCY_ID);
        currency = SqlService.getCurrencySql(getActivity()).getById(currencyId);

        if(identityId==null) {
            certificationBaseAdapter = new CertificationBaseAdapter(getActivity(), new ArrayList<Certification>(), currency);
            setListAdapter(certificationBaseAdapter);
        }else{
            certificationCursorAdapter = new CertificationCursorAdapter(getActivity());
            setListAdapter(certificationCursorAdapter);
        }
        ImageButton certifyButton = (ImageButton) view.findViewById(R.id.certify_button);
        certifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListWallet();
            }
        });

        if(identityId !=null){
            getLoaderManager().initLoader(0, getArguments(), this);
        }
        onRefresh();
    }

    private void majValues(List<Certification> certificationList) {
        if(identityId == null){
            certificationBaseAdapter.swapValues(certificationList);
        }else {
            SqlService.getCertificationSql(getActivity()).insertList(certificationList);
            getLoaderManager().initLoader(0, getArguments(), this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_contact_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
//        final MenuItem searchItem = menu.findItem(R.id.action_lookup);

//        String txt = getArguments().getString(TEXT_SEARCH,"");
//        boolean openSearch = getArguments().getBoolean(OPEN_SEARCH,false);

//        searchView = (SearchView)searchItem.getActionView();
//        searchView.setOnQueryTextListener(this);
//
//        if(openSearch){
//            searchView.setIconified(false);
//            searchView.requestFocus();
//            if(!txt.equals("")){
//                textQuery = txt;
//            }
//        }
//
//        searchView.setQuery(textQuery, true);
//        searchView.clearFocus();
//        if(!textQuery.equals("") || !getArguments().getBoolean(SEE_CONTACT)){
//            searchView.setIconified(false);
//            searchView.requestFocus();
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_lookup:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Certification certif;
        if(identityId == null){
            certif = (Certification) l.getItemAtPosition(position);
        }else{
            Cursor data = (Cursor)getListAdapter().getItem(position);
            certif = SqlService.getCertificationSql(getActivity()).fromCursor(data);
        }

        Contact contact = new Contact();
        contact.setCurrency(currency);
        contact.setUid(certif.getUid());
        contact.setPublicKey(certif.getPublicKey());
        contact.setAlias("");
        contact.setContact(false);

        if(getActivity() instanceof MainActivity){
            Bundle args = new Bundle();
            args.putSerializable(Application.CONTACT, contact);
            ((MainActivity)getActivity()).setCurrentFragment(IdentityFragment.newInstance(args));
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return true;
    }

    @Override
    public void onRefresh() {
        String search = publicKey!=null? publicKey : contact.getPublicKey();
        IdentityService.certiferOf(getActivity(), currency, search, new CallbackCertify() {
            @Override
            public void methode(List<Certification> certificationList) {
                majValues(certificationList);
            }
        });
    }

    private void showListWallet(){
        FragmentManager manager = getFragmentManager();

        ListIdentityDialogFragment dialog = ListIdentityDialogFragment.newInstance(new ListIdentityDialogFragment.Callback() {
            @Override
            public void methode(Identity identity) {
                identitySelected = identity;
                identitySelected.setCurrency(currency);
                IdentityService.certifyIdentity(getActivity(), identity, contact, new CallbackIdentity() {
                    @Override
                    public void methode(Identity identity) {
                        onRefresh();
                    }
                });
            }
        },currencyId);
        dialog.show(manager, "dialog");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (identityId != null) {
            String selection = ViewCertificationAdapter.IDENTITY_ID + "=? AND " +
                    ViewCertificationAdapter.TYPE + "=?";
            String[] selectionArgs = new String[]{String.valueOf(identityId), CertificationType.OF.name()};
            String orderBy = ViewCertificationAdapter.MEDIAN_TIME + " ASC";
            return new CursorLoader(getActivity(), ViewCertificationAdapter.URI, null, selection, selectionArgs, orderBy);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        certificationCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
