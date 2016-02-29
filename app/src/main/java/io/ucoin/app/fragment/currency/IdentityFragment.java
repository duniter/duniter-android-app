package io.ucoin.app.fragment.currency;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.activity.TransferActivity;
import io.ucoin.app.adapter.OperationIdentitySectionAdapter;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.UcoinWallets;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.model.http_api.WotRequirements;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.model.sql.sqlite.Txs;
import io.ucoin.app.model.sql.sqlite.Wallets;
import io.ucoin.app.Format;
import io.ucoin.app.service.RequierementsService;
import io.ucoin.app.service.TxHistoryService;

public class IdentityFragment extends ListFragment
        implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    public static final String TAG = "Identity Fragment";

    public static final int WOT_REQUIEREMENTS = 0;
    public static final int TX_HISTORY = 1;

    private OperationIdentitySectionAdapter operationSectionCursorAdapter;

    private SwipeRefreshLayout mSwipeLayout;
    private ArrayAdapter<UcoinWallet> spinnerAdapter;
    private IdentityContact identityContact;
    private UcoinWallet walletSelected;
    private ImageButton contactButton;
    private ImageView icon;

    private ArrayList<UcoinWallet> listWallet;
    private UcoinCurrency currency;

    private Intent intentRequierementsService;
    private Intent intentHistoryService;

    private TextView textCertification;
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
        if(getActivity() instanceof CurrencyActivity) {
            ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(false);
        }

        if(savedInstanceState!=null){
            getArguments().putSerializable(Application.IDENTITY_CONTACT, savedInstanceState.getSerializable(Application.IDENTITY_CONTACT));
        }

        return inflater.inflate(R.layout.fragment_identity,
                container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Application.IDENTITY_CONTACT, getArguments().getSerializable(Application.IDENTITY_CONTACT));
    }

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiverWotRequierements);
        getActivity().unregisterReceiver(broadcastReceiverTxHistory);
        getActivity().stopService(intentRequierementsService);
        getActivity().stopService(intentHistoryService);
    }

    private BroadcastReceiver broadcastReceiverWotRequierements = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(WOT_REQUIEREMENTS,intent);
        }
    };

    private BroadcastReceiver broadcastReceiverTxHistory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(TX_HISTORY,intent);
        }
    };

    private void updateUI(int type,Intent intent) {
        switch (type){
            case WOT_REQUIEREMENTS:
                Log.d(TAG, "reception requierements");
                WotRequirements requirements = (WotRequirements) intent.getSerializableExtra(RequierementsService.WOT_REQUIEREMENTS);
                updateRequirements(requirements);
                break;
            case TX_HISTORY:
                Log.d(TAG,"reception history");
                Object[] txHistories = (Object[]) intent.getSerializableExtra(TxHistoryService.TX_HISTORY);

                String[] publicKeys = intent.getStringArrayExtra(TxHistoryService.PUBLIC_KEY);
                List<String> test = Arrays.asList(publicKeys);
                for(UcoinWallet wallet:listWallet){
                    wallet.txs().add((TxHistory) txHistories[test.indexOf(wallet.publicKey())]);
                }
                changeWalletSelected();
                break;
            default:
                break;
        }
    }

    private void updateRequirements(WotRequirements requirements){
        identityContact.setRequirements(requirements);

        UcoinCurrency currency = new Currency(getActivity(),identityContact.getCurrencyId());
        int minimum = currency.sigQty();
        int number = identityContact.getRequirements().identities[0].certifications.length;
        textCertification.setText(String.valueOf(number));
        if(minimum>number){
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.red));
            icon.setImageResource(R.drawable.ic_no_member);
        }else{
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.green));
            icon.setImageResource(R.drawable.ic_member);
        }
        String text = textCertification.getText().toString();
        if(number<=1) {
            textCertification.setText(text + " " + getResources().getString(R.string.certification));
        }else{

            textCertification.setText(text + " " + getResources().getString(R.string.certifications));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        intentRequierementsService = new Intent(getActivity(), RequierementsService.class);
        intentHistoryService = new Intent(getActivity(), TxHistoryService.class);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);
        view.findViewById(android.R.id.empty).setOnClickListener(this);
        view.findViewById(R.id.certification).setOnClickListener(this);
        view.findViewById(R.id.information).setOnClickListener(this);
        view.findViewById(R.id.transfer_button).setOnClickListener(this);
        contactButton = (ImageButton) view.findViewById(R.id.contact_button);
        contactButton.setOnClickListener(this);

        TextView alias = (TextView) view.findViewById(R.id.alias);
        TextView publicKey = (TextView) view.findViewById(R.id.public_key);
        textCertification = (TextView) view.findViewById(R.id.txt_certification);
        icon = (ImageView) view.findViewById(R.id.icon_member);


        identityContact = (IdentityContact) getArguments().getSerializable(Application.IDENTITY_CONTACT);

        currency = new Currency(getActivity(), identityContact.getCurrencyId());

        UcoinWallets ucoinWallets = new Wallets(getActivity(),identityContact.getCurrencyId());

        listWallet = ucoinWallets.list();

        String[] listPublicKeyWallet = ucoinWallets.listPublicKey();

        intentRequierementsService.putExtra(RequierementsService.CURRENCY_ID, identityContact.getCurrencyId());
        intentRequierementsService.putExtra(RequierementsService.PUBLIC_KEY, identityContact.getPublicKey());

        intentHistoryService.putExtra(TxHistoryService.CURRENCY_ID, identityContact.getCurrencyId());
        intentHistoryService.putExtra(TxHistoryService.PUBLIC_KEY, listPublicKeyWallet);

        alias.setText(identityContact.toString());
        publicKey.setText(Format.minifyPubkey(identityContact.getPublicKey()));

        spinnerAdapter = new ArrayAdapter<UcoinWallet>(getActivity(),R.layout.spinner_item_wallet,listWallet){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(position==0){
                    return newFirst(parent,false);
                }else{
                    return super.getView(position, convertView, parent);
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if(position==0){
                    return newFirst(parent,true);
                }else{
                    return super.getDropDownView(position, convertView, parent);
                }
            }

            public View newFirst(ViewGroup parent,boolean isDropDown){
                LayoutInflater inflater = (LayoutInflater) getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                View v;

                if(!isDropDown){
                    v = inflater.inflate(R.layout.spinner_item_wallet, parent, false);
                }else{
                    v = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                }
                TextView text = (TextView) v;
                text.setText(getContext().getString(R.string.all));
                return v;
            }

            @Override
            public UcoinWallet getItem(int position) {
                if(position==0){
                    return null;
                }else{
                    return super.getItem(position-1);
                }
            }

            @Override
            public int getCount() {
                return super.getCount()+1;
            }
        };

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        operationSectionCursorAdapter
                = new OperationIdentitySectionAdapter(getActivity(), null, null);
        setListAdapter(operationSectionCursorAdapter);

        if(identityContact.isContact()){
            contactButton.setVisibility(View.GONE);
        }else{
            contactButton.setVisibility(View.VISIBLE);
        }
    }

    public void actionContact(){
        final String uid = identityContact.getUid();
        final String pubKey = identityContact.getPublicKey();
        final Currency currency = new Currency(
                getActivity(),
                identityContact.getCurrencyId());

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
                currency.contacts().add(name, uid, pubKey);
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
        String url = Format.createUri(Format.LONG, uid, pubKey, currency.name());

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
    }

    private void changeWalletSelected(){
        this.walletSelected = (UcoinWallet) spinner.getSelectedItem();
        long value = walletSelected!=null ? walletSelected.id() : -1;
        operationSectionCursorAdapter.swapCursor(new Txs(getActivity()).getByPublicKey(identityContact.getPublicKey(), value).cursor(), walletSelected);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        getActivity().startService(intentRequierementsService);
        getActivity().registerReceiver(broadcastReceiverWotRequierements, new IntentFilter(RequierementsService.BROADCAST_ACTION));
        getActivity().startService(intentHistoryService);
        getActivity().registerReceiver(broadcastReceiverTxHistory, new IntentFilter(TxHistoryService.BROADCAST_ACTION));
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
        Long currencyId = getActivity().getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID);
        intent.putExtra(Application.EXTRA_CURRENCY_ID, currencyId);
        intent.putExtra(Application.EXTRA_WALLET_ID, -1);
        intent.putExtra(Application.EXTRA_IDENTITY, identityContact);
        startActivity(intent);
    }

    private void clickInformation(){
        if(identityContact.getRequirements() != null){
            long value = identityContact.getRequirements().identities[0].meta.timestamp;
            Date date = new Date(value * (long)1000);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            String textDate = getString(R.string.registration_date)
                    .concat(": ")
                    .concat(new SimpleDateFormat("EEE dd MMM yyyy").format(c.getTime()));
            Toast.makeText(getActivity(),textDate,Toast.LENGTH_LONG).show();
        }
    }

    private void clickCertification(){
        if (getActivity() instanceof ActionIdentity) {
            ((ActionIdentity) getActivity()).displayCertification(identityContact.getPublicKey(), identityContact.getCurrencyId());
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

    public interface ActionIdentity{
        public void displayCertification(String publicKey, Long currencyId);
    }
}