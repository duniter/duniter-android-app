package io.ucoin.app.fragment.currency;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.activity.AddContactActivity;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.activity.FindByQrCode;
import io.ucoin.app.adapter.ContactSectionBaseAdapter;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currencies;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.sqlite.SQLiteTable;

public class ContactListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener{

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
    RequestQueue queue;
    ArrayList<IdentityContact> listIdentityContact;
    protected int firstIndexIdentity;
    LoadIdentityTask loadIdentityTask;
    LinearLayout advancedSearch;
    SearchView searchView;

    ContactItemClick listener;


    boolean findByPubKey = false;

    static public ContactListFragment newInstance(Long currencyId,boolean seeContact,boolean addContact){
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(CURRENCY_ID, currencyId);
        newInstanceArgs.putBoolean(SEE_CONTACT,seeContact);
        newInstanceArgs.putBoolean(ADD_CONTACT,addContact);
        ContactListFragment fragment = new ContactListFragment();
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    static public ContactListFragment newInstance(Long currencyId,boolean seeContact,boolean addContact,String txt){
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(CURRENCY_ID, currencyId);
        newInstanceArgs.putBoolean(SEE_CONTACT, seeContact);
        newInstanceArgs.putBoolean(ADD_CONTACT,addContact);
        newInstanceArgs.putString(TEXT_SEARCH,txt);
        newInstanceArgs.putBoolean(OPEN_SEARCH,true);
        ContactListFragment fragment = new ContactListFragment();
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
        if(getActivity() instanceof CurrencyActivity) {
            ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(true);
        }

        return inflater.inflate(R.layout.fragment_contact_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.contacts));
        setHasOptionsMenu(true);

        if(getActivity() instanceof ContactItemClick){
            this.getListView().setOnItemClickListener((ContactItemClick)getActivity());
        }

        listIdentityContact = new ArrayList<>();

        queue = Volley.newRequestQueue(getActivity());

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
    public void onStop() {
        super.onStop();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
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
        searchView.clearFocus();
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
            selection = SQLiteTable.Contact.CURRENCY_ID + "=?";
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
                    selection = SQLiteTable.Contact.PUBLIC_KEY + " LIKE ?";
                } else {
                    selection += " AND " + SQLiteTable.Contact.PUBLIC_KEY + " LIKE ?";
                }
                if (selectionArgs == null) {
                    selectionArgs = new String[]{"%" + query + "%"};
                } else {
                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
                    selectionArgs[selectionArgs.length - 1] = "%" + query + "%";
                }
            }else {
                if (selection == null) {
                    selection = SQLiteTable.Contact.NAME + " LIKE ? OR " + SQLiteTable.Contact.UID + " LIKE ?";
                } else {
                    selection += " AND (" + SQLiteTable.Contact.NAME + " LIKE ? OR " + SQLiteTable.Contact.UID + " LIKE ?)";
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
                    UcoinUris.CONTACT_URI,
                    null,
                    selection,
                    selectionArgs,
                    SQLiteTable.Contact.NAME + " COLLATE NOCASE ASC");
        }
        else{
            return new CursorLoader(getActivity(),
                    UcoinUris.CONTACT_URI,
                    null,
                    SQLiteTable.Contact.PUBLIC_KEY + " LIKE ?",
                    new String[]{"???"},
                    SQLiteTable.Contact.NAME + " COLLATE NOCASE ASC");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        listIdentityContact =new ArrayList<>();
        IdentityContact identityContact;
        if(data.moveToFirst()){
            do{
                Long currencyId = data.getLong(data.getColumnIndex(SQLiteTable.Contact.CURRENCY_ID));
                identityContact =new IdentityContact(
                        true,
                        data.getString(data.getColumnIndex(SQLiteTable.Contact.NAME)),
                        data.getString(data.getColumnIndex(SQLiteTable.Contact.UID)),
                        data.getString(data.getColumnIndex(SQLiteTable.Contact.PUBLIC_KEY)),
                        (new Currency(getActivity(),currencyId)).name(),
                        currencyId);
                listIdentityContact.add(identityContact);
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
        Intent intent = new Intent(getActivity(), AddContactActivity.class);
        intent.putExtra(Application.EXTRA_CURRENCY_ID, currencyId);
        startActivityForResult(intent,215565);
    }

    private void actionScanQrCode(){
        Intent intent = new Intent(getActivity(), FindByQrCode.class);
        intent.putExtra(FindByQrCode.SCAN_QR_CODE, true);
        startActivityForResult(intent, CurrencyActivity.RESULT_SCAN);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
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
            ((ContactSectionBaseAdapter) getListAdapter()).swapList(new ArrayList<IdentityContact>(), false, "", 0);
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
        Comparator<IdentityContact> comparator_contact = new Comparator<IdentityContact>() {

            @Override
            public int compare(IdentityContact o1, IdentityContact o2) {
                if(o1.isContact()){
                    if(o2.isContact()){
                        String i = o1.getName().toLowerCase();
                        String j = o2.getName().toLowerCase();
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

    public class LoadIdentityTask extends AsyncTask<String, Void, String> {

        protected Context mContext;
        protected WotLookup.Result[] results;
        protected Long currencyId;

        public LoadIdentityTask(Context context, Long currencyId){
            this.mContext = context;
            this.currencyId = currencyId;
            firstIndexIdentity = listIdentityContact.size();
        }

        @Override
        protected String doInBackground(String... param) {
            results = null;
            retrieveIdentities();
            return null;
        }

        protected void retrieveIdentities(){
            int socketTimeout = 2000;//2 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            if (currencyId.equals(Long.valueOf(-1))) {
                UcoinCurrencies currencies = new Currencies(Application.getContext());
                Cursor cursor = currencies.getAll();
                UcoinEndpoint endpoint;
                if (cursor.moveToFirst()) {
                    do {
                        Long cId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                        UcoinCurrency currency = new Currency(mContext, cId);
                        endpoint = currency.peers().at(0).endpoints().at(0);
                        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + textQuery.toLowerCase();
                        StringRequest request = request(url, cId);
                        request.setTag(this);
                        request.setRetryPolicy(policy);
                        //Application.getRequestQueue().add(request);
                        queue.add(request);
                    } while (cursor.moveToNext());
                }
            } else {
                UcoinCurrency currency = new Currency(mContext, currencyId);
                UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
                String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + textQuery.toLowerCase();
                StringRequest request = request(url, currencyId);
                request.setTag("TAG");
                request.setRetryPolicy(policy);
                //Application.getRequestQueue().add(request);
                queue.add(request);
            }
        }

        public StringRequest request(String url, final Long id){
            final String name=(new Currency(mContext,id)).name();
            StringRequest request = new StringRequest(
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            WotLookup lookup = WotLookup.fromJson(response);
                            lookupToStringTab(lookup, name, id);
                            majAdapter();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError) {
                                Toast.makeText(Application.getContext(), mContext.getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
                            } else if(error instanceof TimeoutError) {
                                Toast.makeText(Application.getContext(), "Error for connection to "+name, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
                            }
                            majAdapter();
                        }
                    });
            return request;
        }

        public void lookupToStringTab(WotLookup lookup, String currencyName,Long id) {
            IdentityContact identityContact;
            for(WotLookup.Result res : lookup.results){
                identityContact =new IdentityContact(
                        false,
                        "",
                        res.uids[0].uid,
                        res.pubkey,
                        currencyName,
                        id);
                if(identityContact.filter(textQuery,findByPubKey) && !listIdentityContact.contains(identityContact)){
                    listIdentityContact.add(identityContact);
                }
            }
        }
    }

    public interface ContactItemClick extends ListView.OnItemClickListener{
        @Override
        void onItemClick(AdapterView<?> parent, View view, int position, long id);
    }
}
