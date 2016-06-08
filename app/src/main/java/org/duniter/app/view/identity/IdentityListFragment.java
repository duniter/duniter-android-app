package org.duniter.app.view.identity;

import android.app.ListFragment;
import android.content.Intent;
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

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntitySql.ContactSql;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackLookupFilter;
import org.duniter.app.view.FindByQrCode;
import org.duniter.app.view.MainActivity;
import org.duniter.app.view.TransferActivity;
import org.duniter.app.view.identity.adapter.IdentitySectionBaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IdentityListFragment extends ListFragment
        implements
        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    public static final String TEXT_QUERY = "text_query";
    public static final String OPEN_SEARCH = "open_search";
    public static final String FIND_IN_NETWORK = "find_in_network";
    public static final String FIND_BY_PUBLICKEY = "find_by_public_key";
    public static final String POSSIBILITY_ADD_CONTACT = "possibility_add_contact";
    public static final String SEE_CONTACT = "see_contact";

    private static Currency currency;


    ProgressBar progress;
    List<Contact> contacts;
    List<Contact> identities;
    LinearLayout advancedSearch;
    SearchView searchView;


    String textQuery = "";
    boolean openSearch = false;
    boolean findInNetwork = false;
    boolean findByPublicKey = false;
    boolean possibilityAddContact = true;
    boolean seeContact = true;

    ContactSql contactSql = null;
    List<String> listPublicKeyContact = null;

    IdentitySectionBaseAdapter identitySectionBaseAdapter;

    public static IdentityListFragment newInstance(Currency _currency, String textQuery, boolean openSearch, boolean findInNetwork, boolean findByPublicKey, boolean possibilityAddContact, boolean seeContact) {
        currency = _currency;

        Bundle args = new Bundle();
        args.putString(TEXT_QUERY,textQuery);
        args.putBoolean(OPEN_SEARCH,openSearch);
        args.putBoolean(FIND_IN_NETWORK,findInNetwork);
        args.putBoolean(FIND_BY_PUBLICKEY,findByPublicKey);
        args.putBoolean(POSSIBILITY_ADD_CONTACT,possibilityAddContact);
        args.putBoolean(SEE_CONTACT,seeContact);

        IdentityListFragment fragment = new IdentityListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (currency.getName() == null || currency.getName().length()==0){
            currency = SqlService.getCurrencySql(getActivity()).getById(currency.getId());
        }

        textQuery = getArguments().getString(TEXT_QUERY,"");
        openSearch = getArguments().getBoolean(OPEN_SEARCH,false);
        findInNetwork = getArguments().getBoolean(FIND_IN_NETWORK,true);
        findByPublicKey = getArguments().getBoolean(FIND_BY_PUBLICKEY,false);
        possibilityAddContact = getArguments().getBoolean(POSSIBILITY_ADD_CONTACT,true);
        seeContact = getArguments().getBoolean(SEE_CONTACT,true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_contact_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        final MenuItem searchItem = menu.findItem(R.id.action_lookup);

        searchView = (SearchView)searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        if(openSearch){
            searchView.setIconified(false);
            searchView.setQuery(textQuery,false);
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.directory));
        setHasOptionsMenu(true);

        this.getListView().setOnItemClickListener(this);

        progress = (ProgressBar) view.findViewById(R.id.progress_bar);
        progress.bringToFront();
        advancedSearch = (LinearLayout) view.findViewById(R.id.search_advenced);
        ImageButton addContactButton = (ImageButton) view.findViewById(R.id.add_contact_button);
        advancedSearch = (LinearLayout) view.findViewById(R.id.search_advenced);
        Switch switch1 = (Switch) view.findViewById(R.id.switch1);
        switch1.setChecked(findByPublicKey);

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findByPublicKey = isChecked;
                getArguments().putBoolean(FIND_BY_PUBLICKEY, findByPublicKey);
                onQueryTextChange(textQuery);
            }
        });

        identitySectionBaseAdapter = new IdentitySectionBaseAdapter(getActivity(), contacts,identities,this);
        setListAdapter(identitySectionBaseAdapter);

        if(possibilityAddContact) {
            addContactButton.setVisibility(View.VISIBLE);
            addContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionAddContact();
                }
            });
        }else{
            addContactButton.setVisibility(View.GONE);
        }

        initData();
    }

    public void initData(){
        contacts = new ArrayList<>();
        identities = new ArrayList<>();

        if (contactSql == null) {
            contactSql = SqlService.getContactSql(getActivity());
        }

        Map<String,Contact> map;
        if (textQuery==null || textQuery.length()==0){
            map = contactSql.findAllInMap(currency);
        }else {
            if (findByPublicKey) {
                map = contactSql.findByPublicKey(currency, textQuery);
            } else {
                map = contactSql.findByName(currency, textQuery);
            }
        }
        contacts = new ArrayList<>(map.values());
        listPublicKeyContact = new ArrayList<>(map.keySet());

        if (findInNetwork && textQuery.length()>0) {
            IdentityService.getIdentity(getActivity(), currency, textQuery, true, listPublicKeyContact, findByPublicKey, new CallbackLookupFilter() {
                @Override
                public void methode(List<Contact> contacts,String search) {
                    identities = contacts;
                    loadData(search);
                }
            });
        }else{
            loadData(textQuery);
        }
    }

    public void loadData(String search){
        if (textQuery.equals(search)) {
            identitySectionBaseAdapter.swapList(contacts, identities, findInNetwork, textQuery);
            progress.setVisibility(View.GONE);
        }
    }

    private void actionAddContact() {
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
        textQuery = s;
        getArguments().putString(TEXT_QUERY,textQuery);
        progress.setVisibility(View.VISIBLE);

        if(textQuery.equals("")){
            advancedSearch.setVisibility(View.GONE);
        }else{
            advancedSearch.setVisibility(View.VISIBLE);
        }

        findInNetwork = textQuery.length()>=3;
        getArguments().putBoolean(FIND_IN_NETWORK, findInNetwork);

        initData();
        return true;
    }

    public void searchInNetwork(){
        progress.setVisibility(View.VISIBLE);
        searchView.clearFocus();
        getArguments().putBoolean(FIND_IN_NETWORK, true);
        findInNetwork = true;

        initData();
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
}
