package org.duniter.app.view.wallet;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import java.util.ArrayList;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityServices.IdentityService;
import org.duniter.app.model.EntityServices.WalletService;
import org.duniter.app.model.EntitySql.view.ViewTxAdapter;
import org.duniter.app.model.EntitySql.view.ViewWalletIdentityAdapter;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.CallbackIdentity;
import org.duniter.app.technical.format.Formater;
import org.duniter.app.view.MainActivity;
import org.duniter.app.view.TransferActivity;
import org.duniter.app.view.identity.CertificationFragment;
import org.duniter.app.view.identity.IdentityFragment;
import org.duniter.app.view.dialog.InfoDialogFragment;
import org.duniter.app.view.dialog.QrCodeDialogFragment;
import org.duniter.app.view.wallet.adapter.TxCursorAdapter;
import org.duniter.app.view.wallet.adapter.WalletCursorAdapter;

public class WalletFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener{

    public static final String TAG = "Wallet Fragment";

    private static final int WALLET_LOADER_ID = 0;
    private static final int TX_LOADER_ID = 1;

    private SwipeRefreshLayout mSwipeLayout;

    private TextView textCertification;
    private TextView textInformation;

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

    private boolean needSelf;
    private boolean needMembership;
    private boolean willNeedMembership;
    private boolean possibleRevoke;
    private boolean needRenew;
    private int nbCert;
    private int willNeedCertifications;

    private SharedPreferences preferences;

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

    @Override
    public void onResume() {
        super.onResume();
        onRefresh();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
        getActivity().setTitle("");


        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        walletId = getArguments().getLong(Application.WALLET_ID);

        init(view);

        wallet = SqlService.getWalletSql(getActivity()).getById(walletId);

        txCursorAdapter = new TxCursorAdapter(getActivity(), null);
        setListAdapter(txCursorAdapter);

//        WalletService.updateWallet(getActivity(),wallet,false,null);
//        getLoaderManager().initLoader(WALLET_LOADER_ID, getArguments(), this);
//        getLoaderManager().initLoader(TX_LOADER_ID, getArguments(), this);

    }

    private void init(View view){
        alias = (TextView) view.findViewById(R.id.alias);
        secondAmount = (TextView) view.findViewById(R.id.second_amount);
        firstAmount = (TextView) view.findViewById(R.id.principal_amount);
        textCertification = (TextView) view.findViewById(R.id.txt_certification);
        textInformation = (TextView) view.findViewById(R.id.txt_information);

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
        menu.findItem(R.id.action_change).setVisible(false);

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
                callValidationDialog(0);
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
                callValidationDialog(1);
                result = true;
                break;
            case R.id.action_change:
                if (currency == null){
                    currency = SqlService.getCurrencySql(getActivity()).getById(wallet.getCurrency().getId());
                }
                WalletService.testTx(getActivity(),wallet);
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }
        return result;
    }

    private void callValidationDialog(final int type) {// 0 = delete ; 1 = revoke
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.warning))
                .setMessage(type == 0 ? getString(R.string.warning_delete) : getString(R.string.warning_revoke))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (type==0){
                            SqlService.getWalletSql(getActivity()).delete(walletId);
                            getActivity().onBackPressed();
                        }else{
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
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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
        getLoaderManager().restartLoader(WALLET_LOADER_ID, getArguments(), this);
        getLoaderManager().restartLoader(TX_LOADER_ID, getArguments(), this);
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
        List<String> messages = new ArrayList<>();
        if (currency == null){
            currency = SqlService.getCurrencySql(getActivity()).getById(wallet.getCurrency().getId());
        }
        if (identity == null && identityId>0) {
            identity = SqlService.getIdentitySql(getActivity()).getById(identityId);
        }

        int number = Integer.valueOf(identity.getSelfBlockUid().substring(0,identity.getSelfBlockUid().indexOf("-")));

        Bundle args = new Bundle();
        args.putBoolean("needSelf",needSelf);
        args.putBoolean("needMembership",needMembership);
        args.putBoolean("willNeedMembership",willNeedMembership);
        args.putBoolean("needRenew",needRenew);
        args.putInt("willNeedCertifications",willNeedCertifications);

        InfoDialogFragment dial = InfoDialogFragment.newInstance(true,currency,args,number);
        dial.show(getFragmentManager(),InfoDialogFragment.class.getName());
    }

    private void clickCertification(){
        if (!publicKey.equals("") && identityId > 0) {
            if (getActivity() instanceof MainActivity) {
                Bundle args = new Bundle();
                args.putString(Application.PUBLIC_KEY, publicKey);
                args.putLong(Application.IDENTITY_ID, identityId);
                args.putLong(Application.WALLET_ID,walletId);
                args.putString(Application.WALLET_NAME,alias.getText().toString());
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
                boolean display_ud = preferences.getBoolean(Application.DISPLAY_DU,true);
                uri = ViewTxAdapter.URI;
                if (display_ud){
                    selection = ViewTxAdapter.WALLET_ID + "=?";
                    selectionArgs = new String[]{String.valueOf(walletId)};
                }else{
                    selection = ViewTxAdapter.WALLET_ID + "=? AND "+ViewTxAdapter.IS_UD + "=?";
                    selectionArgs = new String[]{String.valueOf(walletId),String.valueOf(false)};
                }
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
                boolean useOblivion = preferences.getBoolean(Application.USE_OBLIVION,true);
                ((TxCursorAdapter) this.getListAdapter()).swapCursor(data,useOblivion);
                break;
        }
    }

    private void updateViewWallet(Cursor cursor){
        int identityIdIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.IDENTITY_ID);
        int sigDateIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.SIG_DATE);
        int currencyIdIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.CURRENCY_ID);
        int publicKeyIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.PUBLIC_KEY);
        int currencyNameIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.CURRENCY_NAME);
        int dividendIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.CURRENT_UD);
        int baseDividendIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.BASE_CURRENT_UD);
        int dtIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.DT);
        int amountIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.AMOUNT);
        int baseIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.BASE);
        int amountTimeWithOblivionIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.AMOUNT_TIME_WITH_OBLIVION);
        int amountTimeWithoutOblivionIndex = cursor.getColumnIndex(ViewWalletIdentityAdapter.AMOUNT_TIME_WITHOUT_OBLIVION);

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

        String currencyName = cursor.getString(currencyNameIndex);
        long amount = cursor.getLong(amountIndex);
        int base = cursor.getInt(baseIndex);

        long dividend = cursor.getLong(dividendIndex);
        int baseDividend = cursor.getInt(baseDividendIndex);

        long amountTimeWithOblivion = cursor.getLong(amountTimeWithOblivionIndex);
        long amountTimeWithoutOblivion =cursor.getLong(amountTimeWithoutOblivionIndex);
        int delay = cursor.getInt(dtIndex);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        boolean useOblivion = preferences.getBoolean(Application.USE_OBLIVION,true);

        int firstUnit = Integer.parseInt(preferences.getString(Application.UNIT, String.valueOf(Application.UNIT_DU)));
        int secondUnit = Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, String.valueOf(Application.UNIT_CLASSIC)));

        if (firstUnit == Application.UNIT_TIME){
            firstAmount.setText(Formater.timeFormatterV2(getActivity(),useOblivion ? amountTimeWithOblivion : amountTimeWithoutOblivion));
        }else{
            Format.initUnit(getActivity(),firstAmount,amount,base,delay,dividend,baseDividend,true,currencyName);
        }

        if (secondUnit == Application.UNIT_TIME){
            secondAmount.setText(Formater.timeFormatterV2(getActivity(),useOblivion ? amountTimeWithOblivion : amountTimeWithoutOblivion));
        }else{
            Format.initUnit(getActivity(),secondAmount,amount,base,delay,dividend,baseDividend,false,currencyName);
        }

//        Format.Currency.changeUnit(
//                getActivity(),
//                currencyName,
//                amount,
//                dividend,
//                delay.intValue(),
//                firstAmount,
//                secondAmount, "");
    }

    private void updateRequirements(int currencySigQty, long nbRequirements, long membership, long membershipPending){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        nbCert = (int)nbRequirements;
        needMembership = membership<=0 && membershipPending <=0;//join
        willNeedMembership = membershipPending>0;
        willNeedCertifications = currencySigQty>nbRequirements ? (int) nbRequirements - currencySigQty : 0;//need nb certification
        needSelf = nbRequirements ==-1; // self
        possibleRevoke = membership>0;
        needRenew = membership>0 && membership<preferences.getLong(Application.RENEW,129600) && membershipPending<=0;

        if (currency == null){
            currency = SqlService.getCurrencySql(getActivity()).getById(wallet.getCurrency().getId());
        }
        if (identity == null && identityId>0) {
            identity = SqlService.getIdentitySql(getActivity()).getById(identityId);
        }

        if (!needSelf &&
                !(needMembership && !willNeedMembership) &&
                !willNeedMembership && !needRenew &&
                !(willNeedCertifications>0)){
            textInformation.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_info, 0, 0);
        }else{
            textInformation.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_info_warning, 0, 0);
        }

        if(needSelf || needMembership || willNeedCertifications>0){
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_red, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.red));
            //icon.setImageResource(R.drawable.ic_no_member);
        }else{
            textCertification.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_certification_green, 0, 0);
            textCertification.setTextColor(getResources().getColor(R.color.green));
            //icon.setImageResource(R.drawable.ic_member);
        }
        String text;
        if(nbRequirements<=1) {
             text = String.format(getString(R.string.Y_certification),String.valueOf(nbRequirements));
        }else{
            text = String.format(getString(R.string.Y_certifications),String.valueOf(nbRequirements));
        }
        textCertification.setText(text);


        signItem.setVisible(needSelf);
        joinItem.setVisible(needMembership && !willNeedMembership);
        revokeItem.setVisible(possibleRevoke);
        renewItem.setVisible(needRenew);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((TxCursorAdapter) this.getListAdapter()).swapCursor(null,false);
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