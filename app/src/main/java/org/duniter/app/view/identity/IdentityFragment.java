package org.duniter.app.view.identity;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Requirement;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntitySql.view.ViewTxAdapter;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackLookup;
import org.duniter.app.technical.callback.CallbackRequirement;
import org.duniter.app.view.MainActivity;
import org.duniter.app.view.TransferActivity;
import org.duniter.app.view.dialog.InfoDialogFragment;
import org.duniter.app.view.identity.adapter.SpinnerWalletArrayAdapter;
import org.duniter.app.view.wallet.adapter.TxCursorAdapter;

public class IdentityFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public static final String TAG = "Identity Fragment";

    public static final int WOT_REQUIEREMENTS = 0;

    private TxCursorAdapter txCursorAdapter;

    private TextView alias;
    private TextView publicKey;

    private SwipeRefreshLayout mSwipeLayout;
    private ArrayAdapter<Wallet> spinnerAdapter;
    private Contact contact;
    private Requirement requirement;
    private Wallet walletSelected;
    private ImageButton contactButton;
    private ImageView icon;

    private List<Wallet> listWallet;
    private Currency currency;

    private TextView textCertification;
    private TextView textInformation;
    private Spinner spinner;

    private int position = 0;

    public static IdentityFragment newInstance(Bundle args) {
        IdentityFragment fragment = new IdentityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if(savedInstanceState!=null){
            getArguments().putSerializable(Application.CONTACT, savedInstanceState.getSerializable(Application.CONTACT));
        }

        return inflater.inflate(R.layout.fragment_identity,
                container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Application.CONTACT, getArguments().getSerializable(Application.CONTACT));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
        Application.hideKeyboard(getActivity(),view);

        init(view);

        contact = (Contact) getArguments().getSerializable(Application.CONTACT);

        currency = contact.getCurrency();

        if (!contact.isContact()) {
            String val = SqlService.getContactSql(getActivity()).isContact(contact.getUid(),contact.getPublicKey(),contact.getCurrency().getId());
            if (val != null){
                contact.setContact(true);
                contact.setAlias(val);
            }
        }

        if (currency.getSigQty()==null){
            currency = SqlService.getCurrencySql(getActivity()).getById(currency.getId());
            contact.setCurrency(currency);
        }

        listWallet = SqlService.getWalletSql(getActivity()).getByCurrency(currency);

        String nom = contact.getAlias().equals("") ? contact.getUid() : contact.getAlias()+" ("+contact.getUid()+")";
        alias.setText(nom);
        publicKey.setText(Format.minifyPubkey(contact.getPublicKey()));

        spinnerAdapter = new SpinnerWalletArrayAdapter(getActivity(),listWallet);

        txCursorAdapter = new TxCursorAdapter(getActivity(), null);
        setListAdapter(txCursorAdapter);

        if(contact.isContact()){
            contactButton.setVisibility(View.GONE);
        }else{
            contactButton.setVisibility(View.VISIBLE);
        }

        updateContact();
    }

    public void init(View view){
        alias = (TextView) view.findViewById(R.id.alias);
        publicKey = (TextView) view.findViewById(R.id.public_key);
        textCertification = (TextView) view.findViewById(R.id.txt_certification);
        textInformation = (TextView) view.findViewById(R.id.txt_information);
        icon = (ImageView) view.findViewById(R.id.icon_member);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);
        view.findViewById(android.R.id.empty).setOnClickListener(this);
        view.findViewById(R.id.certification).setOnClickListener(this);
        view.findViewById(R.id.information).setOnClickListener(this);
        view.findViewById(R.id.transfer_button).setOnClickListener(this);
        contactButton = (ImageButton) view.findViewById(R.id.contact_button);
        contactButton.setOnClickListener(this);
    }

    private void updateContact(){
        IdentityService.getRequirements(getActivity(), currency, contact.getPublicKey(), new CallbackRequirement() {
            @Override
            public void methode(Requirement req) {
                requirement = req;
                if(req == null){
                    updateRequirements(
                            currency.getSigQty().intValue(),
                            0,
                            -1);
                }else {
                    updateRequirements(
                            currency.getSigQty().intValue(),
                            requirement.getNumberCertification(),
                            requirement.getMembershipExpiresIn());
                }
            }
        });
        IdentityService.getIdentity(getActivity(), currency, contact.getPublicKey(), new CallbackLookup() {
            @Override
            public void methode(List<Contact> contactList) {
                contact = contactList.get(0);
            }
        });
    }

    private void updateRequirements(int currencySigQty, long nbRequirements, long membership){
        if(currencySigQty>nbRequirements || membership<=0){
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.red));
            icon.setImageResource(R.drawable.ic_no_member);
        }else{
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.green));
            icon.setImageResource(R.drawable.ic_member);
        }

        String text;
        if(nbRequirements<=1) {
            text = String.format(getString(R.string.Y_certification),String.valueOf(nbRequirements));
        }else{
            text = String.format(getString(R.string.Y_certifications),String.valueOf(nbRequirements));
        }
        textCertification.setText(text);
    }

    public void actionContact(){
        final String uid = contact.getUid();
        final String pubKey = contact.getPublicKey();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Contact");
        alertDialogBuilder.setMessage("Name of contact :");

        final EditText input = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setHint(uid);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String name = input.getText().toString();
                if (name.length() == 0 || name.equals(" ")) {
                    name = uid;
                }
                contact.setAlias(name);
                SqlService.getContactSql(getActivity()).insert(contact);
                askContactInPhone(name, uid, pubKey, currency);
                dialog.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void askContactInPhone(final String name, final String uid, final String pubKey, final Currency currency){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Contact");
        alertDialogBuilder
                .setMessage("Do you want to save the contact on your phone ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addNewContactInPhone(name,uid,pubKey,currency);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void addNewContactInPhone(String name, String uid, String pubKey, Currency currency){
        String url = Format.createUri(Format.LONG, uid, pubKey, currency.getName());

        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);

        ArrayList<ContentValues> data = new ArrayList<ContentValues>();
        ContentValues row1 = new ContentValues();

        row1.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
        row1.put(ContactsContract.CommonDataKinds.Website.URL, url);
        //row1.put(ContactsContract.CommonDataKinds.Website.LABEL, "abc");
        row1.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOME);
        data.add(row1);
        intent.putExtra(ContactsContract.Intents.Insert.DATA, data);
        intent.putExtra("finishActivityOnSaveCompleted", true);
//              Uri dataUri = getActivity().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, row1);
        startActivity(intent);
        contactButton.setVisibility(View.GONE);
        //------------------------------- end of inserting contact in the phone
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_identity, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setGravity(Gravity.RIGHT);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeWalletSelected();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        getActivity().setTitle("");
        changeWalletSelected();
    }

    private void changeWalletSelected(){
        getLoaderManager().initLoader(1, getArguments(), this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        updateContact();
        changeWalletSelected();
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.transfer_button:
                clickTransfer();
                break;
            case R.id.information:
                clickInformation();
                break;
            case R.id.certification:
                clickCertification();
                break;
            case android.R.id.empty:
                clickEmpty();
                break;
            case R.id.contact_button:
                clickContact();
            default:
                clickDefault();
                break;
        }
    }

    private void clickTransfer(){
        Intent intent = new Intent(getActivity(), TransferActivity.class);
        contact.setCurrency(currency);
        intent.putExtra(Application.CONTACT, contact);
        startActivity(intent);
    }

    private void clickInformation(){
        if (requirement!=null) {
            int number = Integer.valueOf(requirement.getSelfBlockUid().substring(0, requirement.getSelfBlockUid().indexOf("-")));
            InfoDialogFragment dial = InfoDialogFragment.newInstance(false, currency, null, number);
            dial.show(getFragmentManager(), InfoDialogFragment.class.getName());
        }
    }

    private void clickCertification(){
        if (getActivity() instanceof MainActivity) {
            Bundle args = new Bundle();
            args.putSerializable(Application.CONTACT, contact);
            args.putLong(Application.CURRENCY_ID, currency.getId());
            ((MainActivity) getActivity()).setCurrentFragment(CertificationFragment.newInstance(args));
        }
    }

    private void clickEmpty(){
        mSwipeLayout.setRefreshing(true);
        onRefresh();
    }

    private void clickContact(){
        actionContact();
    }

    private void clickDefault(){
        Toast.makeText(getActivity(),"En dev",Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        this.walletSelected = (Wallet) spinner.getSelectedItem();
        long value = walletSelected!=null ? walletSelected.getId() : -1;

        String selection;
        String[] selectionArgs;

        if(value == (long)-1){
            selection = ViewTxAdapter.PUBLIC_KEY + "=?";
            selectionArgs = new String[]{contact.getPublicKey()};
        }else{
            selection = ViewTxAdapter.WALLET_ID + "=? AND " +
                    ViewTxAdapter.PUBLIC_KEY + "=?";
            selectionArgs = new String[]{String.valueOf(value),contact.getPublicKey()};
        }

        return new CursorLoader(
                getActivity(),
                ViewTxAdapter.URI,
                null, selection, selectionArgs,
                ViewTxAdapter.TIME + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((TxCursorAdapter) this.getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}