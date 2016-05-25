package io.duniter.app.view.wallet;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.duniter.app.Application;
import io.duniter.app.Format;
import io.duniter.app.R;
import io.duniter.app.model.Entity.Certification;
import io.duniter.app.model.Entity.Contact;
import io.duniter.app.model.Entity.Currency;
import io.duniter.app.model.Entity.Identity;
import io.duniter.app.model.Entity.Tx;
import io.duniter.app.model.Entity.Wallet;
import io.duniter.app.model.Entity.services.IdentityService;
import io.duniter.app.model.Entity.services.WalletService;
import io.duniter.app.model.EntitySql.view.ViewTxAdapter;
import io.duniter.app.model.EntitySql.view.ViewWalletIdentityAdapter;
import io.duniter.app.model.services.SqlService;
import io.duniter.app.technical.callback.CallbackIdentity;
import io.duniter.app.view.MainActivity;
import io.duniter.app.view.TransferActivity;
import io.duniter.app.view.contact.CertificationFragment;
import io.duniter.app.view.contact.IdentityFragment;
import io.duniter.app.view.dialog.QrCodeDialogFragment;
import io.duniter.app.view.wallet.adapter.TxCursorAdapter;

public class WalletFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener{

    public static final String TAG = "Wallet Fragment";

    private static final int WALLET_LOADER_ID = 0;
    private static final int TX_LOADER_ID = 1;

    private SwipeRefreshLayout mSwipeLayout;

    private TextView textCertification;

    private TextView secondAmount;
    private TextView firstAmount;
    private TextView alias;
    private LinearLayout actionTab;

    private TxCursorAdapter txCursorAdapter;

    private long walletId;
    private Wallet wallet;
    private Identity identity;

    private long currencyId = 0;
    private Currency currency;
    private String publicKey = "";
    private long identityId = 0;
    private long sigDate;

    private MenuItem joinItem;
    private MenuItem renewItem;
    private MenuItem revokeItem;
    private MenuItem signItem;


    public static WalletFragment newInstance(Long walletId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(Application.WALLET_ID, walletId);
        WalletFragment fragment = new WalletFragment();
        fragment.setArguments(newInstanceArgs);
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
            getArguments().putLong(Application.WALLET_ID,savedInstanceState.getLong(Application.WALLET_ID));
        }

        return inflater.inflate(R.layout.fragment_wallet,
                container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Application.WALLET_ID, getArguments().getLong(Application.WALLET_ID));
    }

//    private void updateUI(int type,Intent intent) {
//        switch (type){
//            case WOT_REQUIEREMENTS:
//                Log.d(TAG, "reception requierements");
//                WotRequirements requirements = (WotRequirements) intent.getSerializableExtra(RequierementsService.WOT_REQUIEREMENTS);
//                //updateRequirements(requirements);
//                new Identity(getActivity(),identityId).requirements().add(currencyId, requirements);
//                updateRequirements();
//                break;
//            default:
//                break;
//        }
//    }
//
//    private void updateRequirements(){
//        int minimum = currencySigQty!=null ? currencySigQty : 0;
//        textCertification.setText(String.valueOf(nbRequirements));
//        if(minimum>nbRequirements){
//            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
//            textCertification.setTextColor(getResources().getColor(R.color.red));
//            //icon.setImageResource(R.drawable.ic_no_member);
//        }else{
//            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
//            textCertification.setTextColor(getResources().getColor(R.color.green));
//            //icon.setImageResource(R.drawable.ic_member);
//        }
//        String text = textCertification.getText().toString().concat(" ");
//        if(nbRequirements<=1) {
//            textCertification.setText(text.concat(getString(R.string.certification)));
//        }else{
//
//            textCertification.setText(text.concat(getString(R.string.certifications)));
//        }
//    }
//
//    private void updateRequirements(WotRequirements requirements){
//        this.wotRequirements = requirements;
//        int minimum = currencySigQty!=null ? currencySigQty : 0;
//        int number = wotRequirements.identities[0].certifications.length;
//        textCertification.setText(String.valueOf(number));
//        if(minimum>number){
//            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
//            textCertification.setTextColor(getResources().getColor(R.color.red));
//            //icon.setImageResource(R.drawable.ic_no_member);
//        }else{
//            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
//            textCertification.setTextColor(getResources().getColor(R.color.green));
//            //icon.setImageResource(R.drawable.ic_member);
//        }
//        String text = textCertification.getText().toString().concat(" ");
//        if(number<=1) {
//            textCertification.setText(text.concat(getString(R.string.certification)));
//        }else{
//
//            textCertification.setText(text.concat(getString(R.string.certifications)));
//        }
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
        getActivity().setTitle("");

        walletId = getArguments().getLong(Application.WALLET_ID);

        init(view);

        wallet = SqlService.getWalletSql(getActivity()).getById(walletId);

        txCursorAdapter = new TxCursorAdapter(getActivity(), null);
        setListAdapter(txCursorAdapter);

        WalletService.updateWallet(getActivity(),wallet,false,null);
        getLoaderManager().initLoader(WALLET_LOADER_ID, getArguments(), this);
        getLoaderManager().initLoader(TX_LOADER_ID, getArguments(), this);

    }

    private void init(View view){
        alias = (TextView) view.findViewById(R.id.alias);
        secondAmount = (TextView) view.findViewById(R.id.second_amount);
        firstAmount = (TextView) view.findViewById(R.id.principal_amount);
        textCertification = (TextView) view.findViewById(R.id.txt_certification);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeLayout.setOnRefreshListener(this);
        view.findViewById(android.R.id.empty).setOnClickListener(this);
        view.findViewById(R.id.transfer_button).setOnClickListener(this);
        view.findViewById(R.id.certification).setOnClickListener(this);
        view.findViewById(R.id.information).setOnClickListener(this);

        actionTab = (LinearLayout) view.findViewById(R.id.action_tab);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_wallet, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        joinItem = menu.findItem(R.id.action_join);
        renewItem = menu.findItem(R.id.action_renew);
        revokeItem = menu.findItem(R.id.action_revoke);
        signItem = menu.findItem(R.id.action_sign);

        if(identityId <= 0) {
            actionTab.setVisibility(View.GONE);
            return;
        }
        actionTab.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case R.id.action_show_qrcode:
                showQrCode();
                result = true;
                break;
            case R.id.action_delete:
                SqlService.getWalletSql(getActivity()).delete(walletId);
                getActivity().onBackPressed();
                result = true;
                break;
            case R.id.action_sign:
                if (identity == null && identityId>0) {
                    identity = SqlService.getIdentitySql(getActivity()).getById(identityId);
                }
                if (currency == null){
                    currency = SqlService.getCurrencySql(getActivity()).getById(identity.getCurrency().getId());
                }
                identity.setCurrency(currency);
                identity.setWallet(wallet);
                IdentityService.selfIdentity(getActivity(), identity, new CallbackIdentity() {
                    @Override
                    public void methode(Identity identity) {
                        onRefresh();
                    }
                });
                result = true;
                break;
            case R.id.action_join:
            case R.id.action_renew:
                if (identity == null && identityId>0) {
                    identity = SqlService.getIdentitySql(getActivity()).getById(identityId);
                }
                if (currency == null){
                    currency = SqlService.getCurrencySql(getActivity()).getById(identity.getCurrency().getId());
                }
                identity.setCurrency(currency);
                identity.setWallet(wallet);
                IdentityService.joinIdentity(getActivity(), identity, new CallbackIdentity() {
                    @Override
                    public void methode(Identity identity) {
                        onRefresh();
                    }
                });
                result = true;
                break;
            case R.id.action_revoke:
                if (identity == null && identityId>0) {
                    identity = SqlService.getIdentitySql(getActivity()).getById(identityId);
                }
                if (currency == null){
                    currency = SqlService.getCurrencySql(getActivity()).getById(identity.getCurrency().getId());
                }
                identity.setCurrency(currency);
                identity.setWallet(wallet);
                IdentityService.revokeIdentity(getActivity(), identity, new CallbackIdentity() {
                    @Override
                    public void methode(Identity identity) {
                        onRefresh();
                    }
                });
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    private void showQrCode() {
        if (!publicKey.equals("")) {
            QrCodeDialogFragment fragment = QrCodeDialogFragment.newInstance(publicKey);
            fragment.show(getFragmentManager(),
                    fragment.getClass().getSimpleName());
        }
    }

    @Override
    public void onRefresh() {
        WalletService.updateWallet(getActivity(),wallet,true,null);
        getLoaderManager().initLoader(WALLET_LOADER_ID, getArguments(), this);
        getLoaderManager().initLoader(TX_LOADER_ID, getArguments(), this);
        mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case android.R.id.empty:
                clickEmpty();
                break;
            case R.id.transfer_button:
                clickTransfer();
                break;
            case R.id.information:
                clickInformation();
                break;
            case R.id.certification:
                clickCertification();
                break;
            default:
                clickDefault();
                break;
        }
    }

    private void clickInformation(){
        if (identityId > 0){
            Date date = new Date(sigDate * (long)1000);
            String textDate = getString(R.string.registration_date)
                    .concat(": ")
                    .concat(new SimpleDateFormat("EEE dd MMM yyyy").format(date.getTime()));
            Toast.makeText(getActivity(),textDate,Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity(),getString(R.string.hasnt_member),Toast.LENGTH_LONG).show();
        }
    }

    private void clickCertification(){
        if (!publicKey.equals("") && identityId > 0) {
            if (getActivity() instanceof MainActivity) {
                Bundle args = new Bundle();
                args.putString(Application.PUBLIC_KEY, publicKey);
                args.putLong(Application.IDENTITY_ID, identityId);
                args.putLong(Application.CURRENCY_ID, currencyId);
                ((MainActivity) getActivity()).setCurrentFragment(CertificationFragment.newInstance(args));
            }
        }
    }

    private void clickTransfer(){
        if (currencyId>0) {
            Intent intent = new Intent(getActivity(), TransferActivity.class);
            intent.putExtra(Application.WALLET_ID, walletId);
            startActivity(intent);
        }
    }

    private void clickEmpty(){
        mSwipeLayout.setRefreshing(true);
        onRefresh();
    }

    private void clickDefault(){
        Toast.makeText(getActivity(), "En dev", Toast.LENGTH_LONG).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = null;
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = null;
        switch (id){
            case WALLET_LOADER_ID:
                uri = ViewWalletIdentityAdapter.URI;
                selection = ViewWalletIdentityAdapter._ID + "=?";
                selectionArgs = new String[]{String.valueOf(walletId)};
                break;
            case TX_LOADER_ID:
                uri = ViewTxAdapter.URI;
                selection = ViewTxAdapter.WALLET_ID + "=?";
                selectionArgs = new String[]{String.valueOf(walletId)};
                orderBy = ViewTxAdapter.TIME + " DESC";
                break;
        }

        return new CursorLoader(getActivity(),uri,null,selection,selectionArgs,orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case WALLET_LOADER_ID:
                updateViewWallet(data);
                break;
            case TX_LOADER_ID:
                ((TxCursorAdapter) this.getListAdapter()).swapCursor(data);
                break;
        }
    }

    private void updateViewWallet(Cursor cursor){
        int identityIdIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.IDENTITY_ID);
        int sigDateIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.SIG_DATE);
        int currencyIdIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.CURRENCY_ID);
        int publicKeyIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.PUBLIC_KEY);
        int currencyNameIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.CURRENCY_NAME);
        int dividendIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.LAST_UD);
        int dtIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.DT);
        int amountIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.AMOUNT);
        int aliasIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.ALIAS);
        int sigQtyIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.SIG_NEED_QTY);
        int nbRequirementsIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.NB_REQUIREMENTS);
        int membershipIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.MEMBERSHIP);
        int membershipPendingIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.MEMBERSHIP_PENDING);

        if(!cursor.moveToFirst()){
            return;
        }

        alias.setText(cursor.getString(aliasIndex));
        currencyId = cursor.getLong(currencyIdIndex);
        identityId = cursor.getLong(identityIdIndex);
        sigDate = cursor.getLong(sigDateIndex);
        publicKey = cursor.getString(publicKeyIndex);

        long membership = cursor.getLong(membershipIndex);
        long membershipPending = cursor.getLong(membershipPendingIndex);

        int currencySigQty = cursor.getInt(sigQtyIndex);
        long nbRequirements = cursor.getLong(nbRequirementsIndex);
        updateRequirements(currencySigQty,nbRequirements,membership,membershipPending);

        if(identityId <= 0) {
            actionTab.setVisibility(View.GONE);
        }else {
            actionTab.setVisibility(View.VISIBLE);
        }

        Format.Currency.changeUnit(
                getActivity(),
                cursor.getString(currencyNameIndex),
                new BigInteger(cursor.getString(amountIndex)),
                new BigInteger(cursor.getString(dividendIndex)),
                cursor.getInt(dtIndex),
                firstAmount,
                secondAmount, "");
    }

    private void updateRequirements(int currencySigQty, long nbRequirements, long membership, long membershipPending){
        textCertification.setText(String.valueOf(nbRequirements));
        if(currencySigQty>nbRequirements || membership<=0){
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.red));
            //icon.setImageResource(R.drawable.ic_no_member);
        }else{
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.green));
            //icon.setImageResource(R.drawable.ic_member);
        }
        String text = textCertification.getText().toString().concat(" ");
        if(nbRequirements<=1) {
            textCertification.setText(text.concat(getString(R.string.certification)));
        }else{

            textCertification.setText(text.concat(getString(R.string.certifications)));
        }

        boolean needSelf;
        boolean needMembership;
        boolean needRevoke;
        boolean needRenew;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(nbRequirements ==-1){
            needSelf = true;
            needMembership = true;
            needRevoke = false;
            needRenew = false;
        }else{
            needSelf = false;
            needMembership = membership<=0 && membershipPending <=0;
            needRevoke = membership>0;
            needRenew = membership>0 && membership<preferences.getLong(Application.RENEW,129600) && membershipPending<=0;
        }

        signItem.setVisible(needSelf);
        joinItem.setVisible(needMembership);
        revokeItem.setVisible(needRevoke);
        renewItem.setVisible(needRenew);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cursor data = (Cursor)getListAdapter().getItem(position);

        String uid = data.getString(data.getColumnIndex(ViewTxAdapter.UID));
        long currencyId = data.getLong(data.getColumnIndex(ViewTxAdapter.CURRENCY_ID));
        String publicKey = data.getString(data.getColumnIndex(ViewTxAdapter.PUBLIC_KEY));


        if (uid != null && !uid.equals("")) {

            Contact contact = new Contact();
            if (currency == null){
                currency = new Currency(currencyId);
            }
            contact.setCurrency(currency);
            contact.setUid(uid);
            contact.setPublicKey(publicKey);
            contact.setAlias("");
            contact.setContact(false);

            if (getActivity() instanceof MainActivity) {
                Bundle args = new Bundle();
                args.putSerializable(Application.CONTACT, contact);
                ((MainActivity) getActivity()).setCurrentFragment(IdentityFragment.newInstance(args));
            }
        }
    }
}