package io.ucoin.app.fragment.identity;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.os.AsyncTask;
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
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.adapter.CertificationBaseAdapter;
import io.ucoin.app.fragment.dialog.ListWalletDialogFragment;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinIdentities;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.UcoinWallets;
import io.ucoin.app.model.http_api.WotCertification;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.model.sql.sqlite.Identities;
import io.ucoin.app.model.sql.sqlite.Wallets;
import io.ucoin.app.task.SendCertificationTask;

public class CertificationFragment extends ListFragment
        implements SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener,
        ListWalletDialogFragment.Listener {

    private ProgressBar progress;
    private SwipeRefreshLayout mSwipeLayout;
    private String publicKey;
    private Long currencyId;
    private UcoinCurrency currency;
    private CertificationBaseAdapter certificationBaseAdapter;
    private UcoinWallet walletSelected;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (getActivity() instanceof CurrencyActivity) {
            ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(false);
        }

        if (savedInstanceState != null) {
            getArguments().putSerializable(Application.IDENTITY_CONTACT, savedInstanceState.getSerializable(Application.IDENTITY_CONTACT));
        }

        return inflater.inflate(R.layout.fragment_certification_list, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Application.IDENTITY_CONTACT, getArguments().getSerializable(Application.IDENTITY_CONTACT));
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

        publicKey = getArguments().getString(Application.IDENTITY_PUBLICKEY);
        currencyId = getArguments().getLong(Application.IDENTITY_CURRENCY_ID);


        currency = new Currency(getActivity(), currencyId);

        certificationBaseAdapter = new CertificationBaseAdapter(getActivity(), null, currency);
        setListAdapter(certificationBaseAdapter);

        ImageButton certifyButton = (ImageButton) view.findViewById(R.id.certify_button);
        certifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListWallet();
            }
        });

        onRefresh();
    }

    private void majValues(WotCertification wotCertification) {
        certificationBaseAdapter.swapValues(wotCertification);
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
        WotCertification.Certification certif= (WotCertification.Certification) l.getItemAtPosition(position);
        IdentityContact identityContact = new IdentityContact(false,"",certif.uid,certif.pubkey,currency.name(),currencyId);
        if(getActivity() instanceof CurrencyActivity){
            ((CurrencyActivity) getActivity()).displayIdentityFragment(identityContact);
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
        FindWotCertification findNumberCertification = new FindWotCertification(getActivity());
        findNumberCertification.execute();
    }

    @Override
    public void changeWalletSelected(UcoinWallet wallet) {
        if(!wallet.identity().isMember()){
            Toast.makeText(getActivity(),getResources().getString(R.string.hasnt_member),Toast.LENGTH_LONG).show();
            return;
        }
        walletSelected = wallet;
        SendCertificationTask sendCertificationTask = new SendCertificationTask(getActivity(),walletSelected,publicKey);
        sendCertificationTask.execute();
    }

    private void showListWallet(){
        FragmentManager manager = getFragmentManager();

        UcoinWallets wallets = new Wallets(getActivity(),currencyId);

        UcoinIdentities identities = new Identities(getActivity(),currencyId);

        ArrayList<UcoinWallet> listWallet = new ArrayList<>();
        UcoinWallet wallet;
        for(UcoinIdentity identity:identities){
            if(identity.wasMember() && !identity.publicKey().equals(publicKey)){
                wallet = wallets.getByPublicKey(identity.publicKey());
                listWallet.add(wallet);
            }
        }

        ListWalletDialogFragment dialog = new ListWalletDialogFragment(this,listWallet);
        dialog.show(manager, "dialog");
    }

    private class FindWotCertification extends AsyncTask<Void, Integer, Void> {

        protected RequestQueue queue;

        FindWotCertification(Context context) {
            this.queue = Volley.newRequestQueue(context);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            int socketTimeout = 2000;//2 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

            UcoinCurrency currency = new Currency(getActivity(), currencyId);
            UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
            String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/certifiers-of/" + publicKey;

            final StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    cancelQueue();
                    majValues(WotCertification.fromJson(response));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    cancelQueue();
                }
            });
            request.setTag("TAG");
            request.setRetryPolicy(policy);
            queue.add(request);
            return null;
        }

        private void cancelQueue() {
            mSwipeLayout.setRefreshing(false);
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }
}
