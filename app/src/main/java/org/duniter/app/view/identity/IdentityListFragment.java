package org.duniter.app.view.identity;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntitySql.ContactSql;
import org.duniter.app.model.EntitySql.CurrencySql;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackLookup;
import org.duniter.app.view.FindByQrCode;
import org.duniter.app.view.MainActivity;
import org.duniter.app.view.TransferActivity;
import org.duniter.app.view.identity.adapter.ContactSectionBaseAdapter;

public class IdentityListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    private final static String CURRENCY_ID = "currency_id";
    private final static String FIND_IN_NETWORK = "find_in_network";
    private final static String FIND_BY_PUBLICKEY = "find_by_publickey";
    public final static String SEE_CONTACT = "see_contact";
    public final static String ADD_CONTACT = "add_contact";
    public final static String OPEN_SEARCH = "open_search";
    public final static String TEXT_SEARCH = "text_search";

    String textQuery = "";
    boolean findInNetwork = false;
    ProgressBar progress;
    ArrayList<Contact> listIdentityContact;
    protected int firstIndexIdentity;
    LoadIdentityTask loadIdentityTask;
    LinearLayout advancedSearch;
    SearchView searchView;


    boolean findByPubKey = false;

    static public IdentityListFragment newInstance(Long currencyId, boolean seeContact, boolean addContact){
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(CURRENCY_ID, currencyId);
        newInstanceArgs.putBoolean(SEE_CONTACT,seeContact);
        newInstanceArgs.putBoolean(ADD_CONTACT,addContact);
        IdentityListFragment fragment = new IdentityListFragment();
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    static public IdentityListFragment newInstance(Long currencyId, boolean seeContact, boolean addContact, String txt){
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(CURRENCY_ID, currencyId);
        newInstanceArgs.putBoolean(SEE_CONTACT, seeContact);
        newInstanceArgs.putBoolean(ADD_CONTACT,addContact);
        newInstanceArgs.putString(TEXT_SEARCH,txt);
        newInstanceArgs.putBoolean(OPEN_SEARCH,true);
        IdentityListFragment fragment = new IdentityListFragment();
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findInNetwork = getArguments().getBoolean(FIND_IN_NETWORK);
        findByPubKey = getArguments().getBoolean(FIND_BY_PUBLICKEY);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerIndicatorEnabled(true);
        }

        return inflater.inflate(R.layout.list_fragment_contact,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.contacts));
        setHasOptionsMenu(true);

        this.getListView().setOnItemClickListener(this);

        listIdentityContact = new ArrayList<>();

        final Long currencyId = getArguments().getLong(CURRENCY_ID);

        progress = (ProgressBar) view.findViewById(R.id.progress_bar);

        ContactSectionBaseAdapter contactSectionBaseAdapter
                = new ContactSectionBaseAdapter(getActivity(), null,this);
        setListAdapter(contactSectionBaseAdapter);

        ImageButton addContactButton = (ImageButton) view.findViewById(R.id.add_contact_button);

        if(getArguments().getBoolean(ADD_CONTACT)) {
            addContactButton.setVisibility(View.VISIBLE);
            addContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionAddContact(currencyId);
                }
            });
        }else{
            addContactButton.setVisibility(View.GONE);
        }

        advancedSearch = (LinearLayout) view.findViewById(R.id.search_advenced);
        Switch switch1 = (Switch) view.findViewById(R.id.switch1);
        switch1.setChecked(findByPubKey);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getArguments().putBoolean(FIND_BY_PUBLICKEY, isChecked);
                findByPubKey = isChecked;
                onQueryTextChange(textQuery);
            }
        });

        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_contact_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem searchItem = menu.findItem(R.id.action_lookup);

        String txt = getArguments().getString(TEXT_SEARCH,"");
        boolean openSearch = getArguments().getBoolean(OPEN_SEARCH,false);

        searchView = (SearchView)searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        if(openSearch){
            searchView.setIconified(false);
            searchView.requestFocus();
            if(!txt.equals("")){
                textQuery = txt;
            }
        }

        searchView.setQuery(textQuery, true);
//        searchView.clearFocus();
        searchView.requestFocus();
        if(!textQuery.equals("") || !getArguments().getBoolean(SEE_CONTACT)){
            searchView.setIconified(false);
            searchView.requestFocus();
        }
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Long currencyId = args.getLong(CURRENCY_ID);
        String selection = null;
        String[] selectionArgs = null;
        if(!currencyId.equals(Long.valueOf(-1))){
            selection = ContactSql.ContactTable.CURRENCY_ID + "=?";
            selectionArgs = new String[]{currencyId.toString()};
        }

        String query = args.getString("query");
        if(query != null && !query.equals("")) {
            this.textQuery = query;
            if(query.length()>=3){
                args.putBoolean(FIND_IN_NETWORK,true);
                findInNetwork = true;
            }
            if(findByPubKey){
                if (selection == null) {
                    selection = ContactSql.ContactTable.PUBLIC_KEY + " LIKE ?";
                } else {
                    selection += " AND " + ContactSql.ContactTable.PUBLIC_KEY + " LIKE ?";
                }
                if (selectionArgs == null) {
                    selectionArgs = new String[]{"%" + query + "%"};
                } else {
                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
                    selectionArgs[selectionArgs.length - 1] = "%" + query + "%";
                }
            }else {
                if (selection == null) {
                    selection = ContactSql.ContactTable.ALIAS + " LIKE ? OR " + ContactSql.ContactTable.UID + " LIKE ?";
                } else {
                    selection += " AND (" + ContactSql.ContactTable.ALIAS + " LIKE ? OR " + ContactSql.ContactTable.UID + " LIKE ?)";
                }
                if (selectionArgs == null) {
                    selectionArgs = new String[]{query + "%",query + "%"};
                } else {
                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 2);
                    selectionArgs[selectionArgs.length - 1] = query + "%";
                    selectionArgs[selectionArgs.length - 2] = query + "%";
                }
            }
        }else{
            this.textQuery = "";
            args.putBoolean(FIND_IN_NETWORK,false);
            findInNetwork = false;
        }

        if(getArguments().getBoolean(SEE_CONTACT)) {
            return new CursorLoader(getActivity(),
                    ContactSql.URI,
                    null,
                    selection,
                    selectionArgs,
                    ContactSql.ContactTable.ALIAS + " COLLATE NOCASE ASC");
        }
        else{
            return new CursorLoader(getActivity(),
                    ContactSql.URI,
                    null,
                    ContactSql.ContactTable.PUBLIC_KEY + " LIKE ?",
                    new String[]{"???"},
                    ContactSql.ContactTable.ALIAS + " COLLATE NOCASE ASC");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listIdentityContact =new ArrayList<>();
        ContactSql contactSql = SqlService.getContactSql(getActivity());
        CurrencySql currencySql = SqlService.getCurrencySql(getActivity());
        Contact identityContact;
        long currencyId = 0;
        Currency currency = null;
        if(data.moveToFirst()){
            do{
                identityContact = contactSql.fromCursor(data);
                if (!identityContact.getCurrency().getId().equals(currencyId)){
                    currencyId = identityContact.getCurrency().getId();
                    currency = currencySql.getById(currencyId);
                }
                identityContact.setCurrency(currency);
                listIdentityContact.add(contactSql.fromCursor(data));
            }while (data.moveToNext());
        }
        if(listIdentityContact.size()==0 && textQuery.length()>0){
            getArguments().putBoolean(FIND_IN_NETWORK, true);
            findInNetwork = true;
        }
        seeIdentityNetwork();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        getArguments().putBoolean(FIND_IN_NETWORK,false);
        findInNetwork = false;
        onQueryTextChange("");
    }

    private void actionAddContact(Long currencyId) {
        Toast.makeText(getActivity(),getString(R.string.in_dev),Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(getActivity(), AddContactActivity.class);
//        intent.putExtra(Application.CURRENCY_ID, currencyId);
//        startActivityForResult(intent,215565);
    }

    private void actionScanQrCode(){
        Intent intent = new Intent(getActivity(), FindByQrCode.class);
        intent.putExtra(FindByQrCode.SCAN_QR_CODE, true);
        startActivityForResult(intent, MainActivity.RESULT_SCAN);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(loadIdentityTask!=null) {
            loadIdentityTask.cancel(true);
        }
        Bundle args = getArguments();
        args.putLong(CURRENCY_ID, getArguments().getLong(CURRENCY_ID));
        args.putString("query", s);
        getLoaderManager().restartLoader(0, args, this);

        if(s.equals("")){
            advancedSearch.setVisibility(View.GONE);
        }else{
            advancedSearch.setVisibility(View.VISIBLE);
        }
        return true;
    }

    public void searchInNetwork(){
        searchView.clearFocus();
        getArguments().putBoolean(FIND_IN_NETWORK, true);
        findInNetwork =true;
        seeIdentityNetwork();
    }

    public void seeIdentityNetwork(){
        if(findInNetwork) {
            sortContactAndIdentity();
            ((ContactSectionBaseAdapter) getListAdapter()).swapList(new ArrayList<Contact>(), false, "", 0);
        }
        progress.setVisibility(View.VISIBLE);
        if(findInNetwork && !textQuery.equals("")){
            loadIdentityTask = new LoadIdentityTask(
                    getActivity(),
                    getArguments().getLong(CURRENCY_ID));
            loadIdentityTask.execute();
        }else{
            getArguments().putBoolean(FIND_IN_NETWORK,false);
            findInNetwork = false;
            majAdapter();
        }
    }

    public void majAdapter(){
        sortContactAndIdentity();
        ((ContactSectionBaseAdapter) this.getListAdapter()).swapList(listIdentityContact, findInNetwork, textQuery, firstIndexIdentity);
        progress.setVisibility(View.GONE);
    }

    public void sortContactAndIdentity(){
        Comparator<Contact> comparator_contact = new Comparator<Contact>() {

            @Override
            public int compare(Contact o1, Contact o2) {
                if(o1.isContact()){
                    if(o2.isContact()){
                        String i = o1.getAlias().toLowerCase();
                        String j = o2.getAlias().toLowerCase();
                        return i.compareTo(j);
                    }else{
                        return -1;
                    }
                }else{
                    if(o2.isContact()){
                        return 1;
                    }else{
                        String i = o1.getUid().toLowerCase();
                        String j = o2.getUid().toLowerCase();
                        return i.compareTo(j);
                    }
                }
            }

        };
        if(listIdentityContact.size()>0) {
            Collections.sort(listIdentityContact, comparator_contact);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = (Contact) parent.getAdapter().getItem(position);
        if (getActivity() instanceof MainActivity){
            Bundle args = new Bundle();
            args.putSerializable(Application.CONTACT, contact);
            ((MainActivity)getActivity()).setCurrentFragment(IdentityFragment.newInstance(args));
        }else if (getActivity() instanceof TransferActivity){
            ((TransferActivity)getActivity()).setContactSelected(contact);
            getActivity().onBackPressed();
        }
    }

    public class LoadIdentityTask extends AsyncTask<String, Void, String> {

        protected Context mContext;
        protected Long currencyId;

        public LoadIdentityTask(Context context, Long currencyId){
            this.mContext = context;
            this.currencyId = currencyId;
            firstIndexIdentity = listIdentityContact.size();
        }

        @Override
        protected String doInBackground(String... param) {
            retrieveIdentities();
            return null;
        }

        protected void retrieveIdentities(){
            CurrencySql currencySql = SqlService.getCurrencySql(mContext);
            if (!currencyId.equals((long) -1)) {
                Currency currency = currencySql.getById(currencyId);
                IdentityService.getIdentity(mContext, currency, textQuery.toLowerCase(), callbackLookup);
            }else {
                List<Currency> currencyList = currencySql.getAllCurrency();
                for (Currency currency : currencyList) {
                    IdentityService.getIdentity(mContext, currency, textQuery.toLowerCase(), callbackLookup);
                }
            }
        }

        public CallbackLookup callbackLookup = new CallbackLookup() {
            @Override
            public void methode(List<Contact> contactList) {
                lookupToStringTab(contactList);
                majAdapter();
            }
        };

        public void lookupToStringTab(List<Contact> list) {
            for (Contact contact : list){
                if (contact.filter(textQuery.toLowerCase(),findByPubKey) && !listIdentityContact.contains(contact)){
                    listIdentityContact.add(contact);
                }
            }
        }
    }
}
