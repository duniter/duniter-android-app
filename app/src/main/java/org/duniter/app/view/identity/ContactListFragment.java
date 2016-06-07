//package org.duniter.app.view.identity;
//
//import android.app.ListFragment;
//import android.app.LoaderManager;
//import android.content.Context;
//import android.content.CursorLoader;
//import android.content.Intent;
//import android.content.Loader;
//import android.database.Cursor;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.CompoundButton;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.SearchView;
//import android.widget.Switch;
//import android.widget.Toast;
//
//import org.duniter.app.Application;
//import org.duniter.app.R;
//import org.duniter.app.model.Entity.Contact;
//import org.duniter.app.model.Entity.Currency;
//import org.duniter.app.model.EntityServices.IdentityService;
//import org.duniter.app.model.EntitySql.ContactSql;
//import org.duniter.app.model.EntitySql.CurrencySql;
//import org.duniter.app.services.SqlService;
//import org.duniter.app.technical.callback.CallbackLookup;
//import org.duniter.app.view.FindByQrCode;
//import org.duniter.app.view.MainActivity;
//import org.duniter.app.view.TransferActivity;
//import org.duniter.app.view.identity.adapter.ContactSectionBaseAdapter;
//import org.duniter.app.view.identity.adapter.IdentitySectionBaseAdapter;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Map;
//
//public class ContactListFragment extends ListFragment
//        implements
//        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
//
//    public static final String TEXT_QUERY = "text_query";
//    public static final String OPEN_SEARCH = "open_search";
//    public static final String FIND_IN_NETWORK = "find_in_network";
//    public static final String FIND_BY_PUBLICKEY = "find_by_public_key";
//    public static final String POSSIBILITY_ADD_CONTACT = "possibility_add_contact";
//    public static final String SEE_CONTACT = "see_contact";
//
//    private static Currency currency;
//
//
//    ProgressBar progress;
//    ArrayList<Contact> listIdentityContact;
//    protected int firstIndexIdentity;
//    LoadIdentityTask loadIdentityTask;
//    LinearLayout advancedSearch;
//    SearchView searchView;
//
//
//    String textQuery = "";
//    boolean openSearch = false;
//    boolean findInNetwork = false;
//    boolean findByPublicKey = false;
//    boolean possibilityAddContact = true;
//    boolean seeContact = true;
//
//    ContactSql contactSql = null;
//    Map<String,String> mapContact = null;
//
//    IdentitySectionBaseAdapter identitySectionBaseAdapter;
//
//    public static ContactListFragment newInstance(Currency _currency, String textQuery, boolean openSearch, boolean findInNetwork, boolean findByPublicKey, boolean possibilityAddContact, boolean seeContact) {
//        currency = _currency;
//
//        Bundle args = new Bundle();
//        args.putString(TEXT_QUERY,textQuery);
//        args.putBoolean(OPEN_SEARCH,openSearch);
//        args.putBoolean(FIND_IN_NETWORK,findInNetwork);
//        args.putBoolean(FIND_BY_PUBLICKEY,findByPublicKey);
//        args.putBoolean(POSSIBILITY_ADD_CONTACT,possibilityAddContact);
//        args.putBoolean(SEE_CONTACT,seeContact);
//
//        ContactListFragment fragment = new ContactListFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (currency.getName() == null || currency.getName().length()==0){
//            currency = SqlService.getCurrencySql(getActivity()).getById(currency.getId());
//        }
//
//        textQuery = getArguments().getString(TEXT_QUERY,"");
//        openSearch = getArguments().getBoolean(OPEN_SEARCH,false);
//        findInNetwork = getArguments().getBoolean(FIND_IN_NETWORK,true);
//        findByPublicKey = getArguments().getBoolean(FIND_BY_PUBLICKEY,false);
//        possibilityAddContact = getArguments().getBoolean(POSSIBILITY_ADD_CONTACT,true);
//        seeContact = getArguments().getBoolean(SEE_CONTACT,true);
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        super.onCreateView(inflater, container, savedInstanceState);
//        if(getActivity() instanceof MainActivity) {
//            ((MainActivity) getActivity()).setDrawerIndicatorEnabled(true);
//        }
//
//        return inflater.inflate(R.layout.list_fragment_contact,
//                container, false);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.toolbar_contact_list, menu);
//    }
//
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        final MenuItem searchItem = menu.findItem(R.id.action_lookup);
//
//        searchView = (SearchView)searchItem.getActionView();
//        searchView.setOnQueryTextListener(this);
//
//        if(openSearch){
//            searchView.setIconified(false);
//            searchView.requestFocus();
//            searchView.setQuery(textQuery,true);
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.action_lookup:
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        getActivity().setTitle(getString(R.string.directory));
//        setHasOptionsMenu(true);
//
//        this.getListView().setOnItemClickListener(this);
//
//        listIdentityContact = new ArrayList<>();
//        progress = (ProgressBar) view.findViewById(R.id.progress_bar);
//        advancedSearch = (LinearLayout) view.findViewById(R.id.search_advenced);
//        ImageButton addContactButton = (ImageButton) view.findViewById(R.id.add_contact_button);
//        advancedSearch = (LinearLayout) view.findViewById(R.id.search_advenced);
//        Switch switch1 = (Switch) view.findViewById(R.id.switch1);
//        switch1.setChecked(findByPublicKey);
//
//        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                findByPublicKey = isChecked;
//                getArguments().putBoolean(FIND_BY_PUBLICKEY, findByPublicKey);
//                onQueryTextChange(textQuery);
//            }
//        });
//
//        identitySectionBaseAdapter = new IdentitySectionBaseAdapter(getActivity(), null);
//        setListAdapter(identitySectionBaseAdapter);
//
//        if(possibilityAddContact) {
//            addContactButton.setVisibility(View.VISIBLE);
//            addContactButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    actionAddContact();
//                }
//            });
//        }else{
//            addContactButton.setVisibility(View.GONE);
//        }
//
//        initData();
//    }
//
//    public void initData(){
//        if (contactSql == null){
//            contactSql = SqlService.getContactSql(getActivity());
//        }
//
//        if (mapContact == null){
//            mapContact = contactSql.getMap(currency.getId());
//        }
//
//        if (findInNetwork) {
//            IdentityService.getIdentity(getActivity(), currency, textQuery, new CallbackLookup() {
//                @Override
//                public void methode(List<Contact> contacts) {
//                    for (Contact contact : contacts) {
//                        if (mapContact.containsKey(contact.getPublicKey())) {
//                            contact.setAlias(mapContact.get(contact.getPublicKey()));
//                        }
//                    }
//                    loadData(contacts);
//                }
//            });
//        }else{
//            List<Contact> contacts;
//            if (findByPublicKey){
//                contacts = contactSql.findByPublicKey(currency.getId(),textQuery);
//            }else{
//                contacts = contactSql.findByName(currency.getId(),textQuery);
//            }
//            loadData(contacts);
//        }
//    }
//
//    public void loadData(List<Contact> contacts){
//        identitySectionBaseAdapter.swapList(contacts,findInNetwork,textQuery);
//        progress.setVisibility(View.GONE);
//    }
//
////    @Override
////    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
////        Long currencyId = args.getLong(CURRENCY_ID);
////        String selection = null;
////        String[] selectionArgs = null;
////        if(!currencyId.equals(Long.valueOf(-1))){
////            selection = ContactSql.ContactTable.CURRENCY_ID + "=?";
////            selectionArgs = new String[]{currencyId.toString()};
////        }
////
////        String query = args.getString("query");
////        if(query != null && !query.equals("")) {
////            this.textQuery = query;
////            if(query.length()>=3){
////                args.putBoolean(FIND_IN_NETWORK,true);
////                findInNetwork = true;
////            }
////            if(findByPubKey){
////                if (selection == null) {
////                    selection = ContactSql.ContactTable.PUBLIC_KEY + " LIKE ?";
////                } else {
////                    selection += " AND " + ContactSql.ContactTable.PUBLIC_KEY + " LIKE ?";
////                }
////                if (selectionArgs == null) {
////                    selectionArgs = new String[]{"%" + query + "%"};
////                } else {
////                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
////                    selectionArgs[selectionArgs.length - 1] = "%" + query + "%";
////                }
////            }else {
////                if (selection == null) {
////                    selection = ContactSql.ContactTable.ALIAS + " LIKE ? OR " + ContactSql.ContactTable.UID + " LIKE ?";
////                } else {
////                    selection += " AND (" + ContactSql.ContactTable.ALIAS + " LIKE ? OR " + ContactSql.ContactTable.UID + " LIKE ?)";
////                }
////                if (selectionArgs == null) {
////                    selectionArgs = new String[]{query + "%",query + "%"};
////                } else {
////                    selectionArgs = Arrays.copyOf(selectionArgs, selectionArgs.length + 2);
////                    selectionArgs[selectionArgs.length - 1] = query + "%";
////                    selectionArgs[selectionArgs.length - 2] = query + "%";
////                }
////            }
////        }else{
////            this.textQuery = "";
////            args.putBoolean(FIND_IN_NETWORK,false);
////            findInNetwork = false;
////        }
////
////        if(getArguments().getBoolean(SEE_CONTACT)) {
////            return new CursorLoader(getActivity(),
////                    ContactSql.URI,
////                    null,
////                    selection,
////                    selectionArgs,
////                    ContactSql.ContactTable.ALIAS + " COLLATE NOCASE ASC");
////        }
////        else{
////            return new CursorLoader(getActivity(),
////                    ContactSql.URI,
////                    null,
////                    ContactSql.ContactTable.PUBLIC_KEY + " LIKE ?",
////                    new String[]{"???"},
////                    ContactSql.ContactTable.ALIAS + " COLLATE NOCASE ASC");
////        }
////    }
////
////    @Override
////    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
////        listIdentityContact =new ArrayList<>();
////        ContactSql contactSql = SqlService.getContactSql(getActivity());
////        CurrencySql currencySql = SqlService.getCurrencySql(getActivity());
////        Contact identityContact;
////        long currencyId = 0;
////        Currency currency = null;
////        if(data.moveToFirst()){
////            do{
////                identityContact = contactSql.fromCursor(data);
////                if (!identityContact.getCurrency().getId().equals(currencyId)){
////                    currencyId = identityContact.getCurrency().getId();
////                    currency = currencySql.getById(currencyId);
////                }
////                identityContact.setCurrency(currency);
////                listIdentityContact.add(contactSql.fromCursor(data));
////            }while (data.moveToNext());
////        }
////        if(listIdentityContact.size()==0 && textQuery.length()>0){
////            getArguments().putBoolean(FIND_IN_NETWORK, true);
////            findInNetwork = true;
////        }
////        seeIdentityNetwork();
////    }
////
////    @Override
////    public void onLoaderReset(Loader<Cursor> loader) {
////        getArguments().putBoolean(FIND_IN_NETWORK,false);
////        findInNetwork = false;
////        onQueryTextChange("");
////    }
//
//    private void actionAddContact(Long currencyId) {
//        Toast.makeText(getActivity(),getString(R.string.in_dev),Toast.LENGTH_SHORT).show();
////        Intent intent = new Intent(getActivity(), AddContactActivity.class);
////        intent.putExtra(Application.CURRENCY_ID, currencyId);
////        startActivityForResult(intent,215565);
//    }
//
//    private void actionScanQrCode(){
//        Intent intent = new Intent(getActivity(), FindByQrCode.class);
//        intent.putExtra(FindByQrCode.SCAN_QR_CODE, true);
//        startActivityForResult(intent, MainActivity.RESULT_SCAN);
//    }
//
//    @Override
//    public boolean onQueryTextSubmit(String s) {
//        return false;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String s) {
//        textQuery = s;
//        getArguments().putString(TEXT_QUERY,textQuery);
//        progress.setVisibility(View.VISIBLE);
//
//        if(textQuery.equals("")){
//            advancedSearch.setVisibility(View.GONE);
//        }else{
//            advancedSearch.setVisibility(View.VISIBLE);
//        }
//        initData();
//        return true;
//    }
////
////    public void searchInNetwork(){
////        searchView.clearFocus();
////        getArguments().putBoolean(FIND_IN_NETWORK, true);
////        findInNetwork =true;
////        seeIdentityNetwork();
////    }
////
////    public void seeIdentityNetwork(){
////        if(findInNetwork) {
////            sortContactAndIdentity();
////            ((ContactSectionBaseAdapter) getListAdapter()).swapList(new ArrayList<Contact>(), false, "", 0);
////        }
////        progress.setVisibility(View.VISIBLE);
////        if(findInNetwork && !textQuery.equals("")){
////            loadIdentityTask = new LoadIdentityTask(
////                    getActivity(),
////                    getArguments().getLong(CURRENCY_ID));
////            loadIdentityTask.execute();
////        }else{
////            getArguments().putBoolean(FIND_IN_NETWORK,false);
////            findInNetwork = false;
////            majAdapter();
////        }
////    }
////
////    public void majAdapter(){
////        sortContactAndIdentity();
////        ((ContactSectionBaseAdapter) this.getListAdapter()).swapList(listIdentityContact, findInNetwork, textQuery, firstIndexIdentity);
////        progress.setVisibility(View.GONE);
////    }
//
////    public void sortContactAndIdentity(){
////        Comparator<Contact> comparator_contact = new Comparator<Contact>() {
////
////            @Override
////            public int compare(Contact o1, Contact o2) {
////                if(o1.isContact()){
////                    if(o2.isContact()){
////                        String i = o1.getAlias().toLowerCase();
////                        String j = o2.getAlias().toLowerCase();
////                        return i.compareTo(j);
////                    }else{
////                        return -1;
////                    }
////                }else{
////                    if(o2.isContact()){
////                        return 1;
////                    }else{
////                        String i = o1.getUid().toLowerCase();
////                        String j = o2.getUid().toLowerCase();
////                        return i.compareTo(j);
////                    }
////                }
////            }
////
////        };
////        if(listIdentityContact.size()>0) {
////            Collections.sort(listIdentityContact, comparator_contact);
////        }
////    }
//
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Contact contact = (Contact) parent.getAdapter().getItem(position);
//        if (getActivity() instanceof MainActivity){
//            Bundle args = new Bundle();
//            args.putSerializable(Application.CONTACT, contact);
//            ((MainActivity)getActivity()).setCurrentFragment(IdentityFragment.newInstance(args));
//        }else if (getActivity() instanceof TransferActivity){
//            ((TransferActivity)getActivity()).setContactSelected(contact);
//            getActivity().onBackPressed();
//        }
//    }
//
////    public class LoadIdentityTask extends AsyncTask<String, Void, String> {
////
////        protected Context mContext;
////        protected Long currencyId;
////
////        public LoadIdentityTask(Context context, Long currencyId){
////            this.mContext = context;
////            this.currencyId = currencyId;
////            firstIndexIdentity = listIdentityContact.size();
////        }
////
////        @Override
////        protected String doInBackground(String... param) {
////            retrieveIdentities();
////            return null;
////        }
////
////        protected void retrieveIdentities(){
////            CurrencySql currencySql = SqlService.getCurrencySql(mContext);
////            if (!currencyId.equals((long) -1)) {
////                Currency currency = currencySql.getById(currencyId);
////                IdentityService.getIdentity(mContext, currency, textQuery.toLowerCase(), callbackLookup);
////            }else {
////                List<Currency> currencyList = currencySql.getAllCurrency();
////                for (Currency currency : currencyList) {
////                    IdentityService.getIdentity(mContext, currency, textQuery.toLowerCase(), callbackLookup);
////                }
////            }
////        }
////
////        public CallbackLookup callbackLookup = new CallbackLookup() {
////            @Override
////            public void methode(List<Contact> contactList) {
////                lookupToStringTab(contactList);
////                majAdapter();
////            }
////        };
////
////        public void lookupToStringTab(List<Contact> list) {
////            for (Contact contact : list){
////                if (contact.filter(textQuery.toLowerCase(),findByPubKey) && !listIdentityContact.contains(contact)){
////                    listIdentityContact.add(contact);
////                }
////            }
////        }
////    }
//}
