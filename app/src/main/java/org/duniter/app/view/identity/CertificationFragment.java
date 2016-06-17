package org.duniter.app.view.identity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import java.util.Map;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.enumeration.CertificationType;
import org.duniter.app.model.Entity.Certification;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Wallet;
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
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener{

    private ProgressBar                progress;
    private SwipeRefreshLayout mSwipeLayout;
    private String                     publicKey;
    private Long                       identityId;
    private Long                       walletId;
    private Long                       currencyId;
    private Currency currency;
    private Contact contact;
    private CertificationBaseAdapter   certificationBaseAdapter;
    private CertificationCursorAdapter certificationCursorAdapter;
    private Identity identitySelected;
    private ImageButton certifyButton;

    private List<String> certifier;
    private Map<String,Identity> identities;

    private View.OnClickListener clickCertificationAccepted;
    private View.OnClickListener clickCertificationRefused;

    static public CertificationFragment newInstance(Bundle args) {
        CertificationFragment fragment = new CertificationFragment();
        fragment.setArguments(args);
        return fragment;
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

        getActivity().setTitle(getString(R.string.certification_received));

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
        identityId = getArguments().getLong(Application.IDENTITY_ID);
        walletId = getArguments().getLong(Application.WALLET_ID);

        if(identityId==0) {
            identities = SqlService.getIdentitySql(getActivity()).getMapByCurrency(currencyId);
            certificationBaseAdapter = new CertificationBaseAdapter(getActivity(), new ArrayList<Certification>(), currency);
            setListAdapter(certificationBaseAdapter);
        }else{
            identities = SqlService.getIdentitySql(getActivity()).getMapByCurrencyWithoutId(currencyId,walletId);
            certificationCursorAdapter = new CertificationCursorAdapter(getActivity());
            setListAdapter(certificationCursorAdapter);
        }

        clickCertificationAccepted = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showListWallet();
            }
        };

        clickCertificationRefused = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMessage();
            }
        };


        certifyButton = (ImageButton) view.findViewById(R.id.certify_button);

        onRefresh();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Certification certif;
        Contact contact = new Contact();
        contact.setCurrency(currency);
        if(identityId == 0){
            certif = (Certification) l.getItemAtPosition(position);
            contact.setUid(certif.getUid());
            contact.setPublicKey(certif.getPublicKey());
            contact.setAlias("");
            contact.setContact(false);
        }else{
            Cursor data = (Cursor)getListAdapter().getItem(position);
            contact.setUid(data.getString(data.getColumnIndex(ViewCertificationAdapter.UID)));
            contact.setPublicKey(data.getString(data.getColumnIndex(ViewCertificationAdapter.PUBLIC_KEY)));
            String alias = data.getString(data.getColumnIndex(ViewCertificationAdapter.ALIAS));
            contact.setAlias(alias!=null ? alias : "");
            contact.setContact(alias!=null);
        }

        if(getActivity() instanceof MainActivity){
            Bundle args = new Bundle();
            args.putSerializable(Application.CONTACT, contact);
            ((MainActivity)getActivity()).setCurrentFragment(IdentityFragment.newInstance(args));
        }
    }

    @Override
    public void onRefresh() {
        String search = publicKey!=null? publicKey : contact.getPublicKey();
        if (identityId==0) {
            IdentityService.certiferOf(getActivity(), currency, search, new CallbackCertify() {
                @Override
                public void methode(List<Certification> certificationList) {
                    certificationBaseAdapter.swapValues(certificationList);
                    mSwipeLayout.setRefreshing(false);
                    certifier = new ArrayList<String>();
                    for (Certification c : certificationList){
                        certifier.add(c.getPublicKey());
                    }
                    showButtonCertify();
                }
            });
        }else{
            getLoaderManager().restartLoader(0, getArguments(), this);
            showButtonCertify();
        }
    }

    private void showListWallet(){
        final CertificationFragment f = this;
        FragmentManager manager = getFragmentManager();

        ListIdentityDialogFragment dialog = ListIdentityDialogFragment.newInstance(new ListIdentityDialogFragment.Callback() {
            @Override
            public void methode(Identity identity) {
                identitySelected = identity;
                identitySelected.setCurrency(currency);
                //TODO password
                Wallet wallet = SqlService.getWalletSql(getActivity()).getById(identitySelected.getWallet().getId());
                identitySelected.setWallet(wallet);
                IdentityService.certifyIdentity(getActivity(), identitySelected, contact, new CallbackIdentity() {
                    @Override
                    public void methode(Identity identity) {
                        f.onRefresh();
                    }
                });
            }
        },currencyId,new ArrayList<>(identities.values()));
        dialog.show(manager, "dialog");
    }

    private void showMessage(){
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.already_certified))
                .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (identityId != 0) {
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
        certificationCursorAdapter.swapCursor(data);
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void showButtonCertify(){
        if (identityId == 0 && walletId == 0){
            if (certifier!=null){
                for (String key : certifier){
                    if (identities.containsKey(key)){
                        identities.remove(key);
                    }
                }
            }
        }
        if (identities.size()==0){
            if (walletId != 0){
                certifyButton.setVisibility(View.GONE);
            }else{
                certifyButton.setVisibility(View.VISIBLE);
            }
            certifyButton.setOnClickListener(clickCertificationRefused);
        }else{
            certifyButton.setOnClickListener(clickCertificationAccepted);
        }
    }
}
