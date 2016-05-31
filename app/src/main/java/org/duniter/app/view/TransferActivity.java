package org.duniter.app.view;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.duniter.app.Application;
import org.duniter.app.Format;
import org.duniter.app.R;
import org.duniter.app.enumeration.TxState;
import org.duniter.app.model.Entity.Contact;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Source;
import org.duniter.app.model.Entity.Tx;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityServices.WalletService;
import org.duniter.app.model.EntitySql.SourceSql;
import org.duniter.app.model.document.TxDoc;
import org.duniter.app.services.SqlService;
import org.duniter.app.services.WebService;
import org.duniter.app.technical.crypto.AddressFormatException;
import org.duniter.app.view.identity.IdentityListFragment;
import org.duniter.app.view.dialog.ConverterDialog;
import org.duniter.app.view.wallet.WalletListFragment;

public class TransferActivity extends ActionBarActivity implements View.OnClickListener {

    /*
        https://github.com/ucoin-io/ucoin/blob/master/doc/Protocol.md#validity-1
        Field Comment is a string of maximum 255 characters, exclusively composed of
        alphanumeric characters, space, - _ : / ; * [ ] ( ) ? ! ^ + = @ & ~ # { } | \ < > % .
    */
    private static final String COMMENT_REGEX = "^[\\p{Alnum}\\p{Space}{\\-_:/;\\*\\[\\]\\(\\)\\?\\!\\^\\+=@&~\\#\\{\\}\\|\\\\<>%\\.}]{0,255}";
    private static final String AMOUNT_REGEX = "^[0-9]{1,3}(\\.[0-9]{0,8})?$";
    private static final String PUBLIC_KEY_REGEX = "[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{43,44}$";
    public static final String SEARCH_IDENTITY = "search_identity";
    private TextView mWalletAlias;
    private TextView mWalletAmount;
    private TextView mWalletDefaultAmount;
    private TextView mContact;
    private TextView defaultAmount;
    private TextView noDataWallet;

    private EditText mReceiverPublicKey;
    private EditText amount;
    private EditText mComment;

    private LinearLayout layoutTansfer;
    private LinearLayout dataWallet;

    private MenuItem mTransferMenuItem;
    private Spinner spinnerUnit;

    private int unit;
    private int defaultUnit;

    private Currency currency;
    private BigInteger dividend;
    private int dt;
    private Wallet walletSelected;
    private Contact contactSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transfer);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        unit = Integer.parseInt(preferences.getString(Application.UNIT, Application.UNIT_CLASSIC + ""));
        defaultUnit = Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, Application.UNIT_CLASSIC + ""));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        try {
            setSupportActionBar(toolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        if(unit == Application.UNIT_TIME){
            List list = Arrays.asList(getResources().getStringArray(R.array.list_unit_time));
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, list);
            spinnerUnit.setAdapter(dataAdapter);
            spinnerUnit.setSelection(Format.Time.MINUTE);
            spinnerUnit.setVisibility(View.VISIBLE);
            spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    majDefaultAmount();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }else{
            spinnerUnit.setVisibility(View.GONE);
        }


        amount.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (walletSelected == null){
                    dataWallet.requestFocus();
                }else{
                    majDefaultAmount();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Contact contact = (Contact) getIntent().getExtras().getSerializable(Application.CONTACT);
        setContactSelected(contact);
        long walletId = getIntent().getExtras().getLong(Application.WALLET_ID,-1);
        setWalletSelected(walletId);

        TextView amount_label = (TextView) findViewById(R.id.amount_label);

        switch (unit){
            case Application.UNIT_CLASSIC:
                amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                amount_label.setText(amount_label.getText()+" "+Format.Currency.unitCurrency(currency.getName()));
                break;
            case Application.UNIT_DU:
                amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                amount_label.setText(amount_label.getText()+" "+getString(R.string.ud));
                break;
            case Application.UNIT_TIME:
                amount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                amount_label.setText(amount_label.getText()+" "+getString(R.string.time));
                break;
        }

    }

    private void init(){
        final LinearLayout mWallet = (LinearLayout) findViewById(R.id.wallet);
        mWallet.setOnClickListener(this);

        RelativeLayout layoutContact = (RelativeLayout) findViewById(R.id.layout_contact);
        layoutContact.setOnClickListener(this);

        RelativeLayout layoutAmount = (RelativeLayout) findViewById(R.id.layout_amount);
        layoutAmount.setOnClickListener(this);

        layoutTansfer = (LinearLayout) findViewById(R.id.layout_transfer);
        dataWallet = (LinearLayout) findViewById(R.id.data_wallet);
        noDataWallet = (TextView) findViewById(R.id.no_data_wallet);

        mContact = (TextView) findViewById(R.id.contact);

        ImageButton mCalculate = (ImageButton) findViewById(R.id.action_calcul);
        mCalculate.setOnClickListener(this);

        ImageButton mSearchButton = (ImageButton) findViewById(R.id.action_lookup);
        mSearchButton.setOnClickListener(this);

        ImageButton scanQrCode = (ImageButton) findViewById(R.id.action_scan_qrcode);
        scanQrCode.setOnClickListener(this);

        mWalletAlias = (TextView) findViewById(R.id.wallet_alias);
        mWalletAmount = (TextView) findViewById(R.id.wallet_amount);
        mWalletDefaultAmount = (TextView) findViewById(R.id.wallet_default_amount);

        mReceiverPublicKey = (EditText) findViewById(R.id.receiver_public_key);

        amount = (EditText) findViewById(R.id.amount);

        defaultAmount = (TextView) findViewById(R.id.second_amount);
        spinnerUnit = (Spinner) findViewById(R.id.spinner_unit);

        mComment = (EditText) findViewById(R.id.comment);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                char[] chars = {'\'','"'};
                for (int i = start; i < end; i++) {
                    if (new String(chars).contains(String.valueOf(source.charAt(i)))) {
                        return "";
                    }
                }
                return null;
            }
        };
        mComment.setFilters(new InputFilter[] { filter });
    }

    private void majDefaultAmount(){
        String val = amount.getText().toString();
        if(val.equals("") || val.equals(" ") || val.equals(".")){
            val="0";
        }
        if(val.substring(0,1).equals(".")){
            val="0"+val;
        }
        if(unit!=defaultUnit) {
            BigInteger quantitative = null;
            switch (unit){
                case Application.UNIT_CLASSIC:
                    quantitative = new BigInteger(val);
                    break;
                case Application.UNIT_DU:
                    quantitative = Format.Currency.relativeToQuantitative(this, new BigDecimal(val), dividend);
                    break;
                case Application.UNIT_TIME:
                    val = Format.Time.toSecond(this, new BigDecimal(val), spinnerUnit.getSelectedItemPosition()).toString();
                    quantitative = Format.Currency.timeToQuantitative(this, new BigDecimal(val), dt, dividend);
                    break;
            }
            Format.Currency.changeUnit(
                    this,
                    currency.getName(),
                    quantitative,
                    dividend,
                    dt,
                    null,
                    defaultAmount,
                    "");
        }
    }

    private BigInteger toQuantitative(BigDecimal val){
        BigInteger res = null;
        if(currency!=null) {
            switch (unit) {
                case Application.UNIT_CLASSIC:
                    res = val.toBigInteger();
                    break;
                case Application.UNIT_DU:
                    res = Format.Currency.relativeToQuantitative(this, val, dividend);
                    break;
                case Application.UNIT_TIME:
                    val = Format.Time.toSecond(this, val, spinnerUnit.getSelectedItemPosition());
                    res = Format.Currency.timeToQuantitative(this, val, dt, dividend);
                    break;
            }
        }
        return res;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_transfer, menu);
        mTransferMenuItem = menu.findItem(R.id.action_transfer);
        int bsEntryCount = getFragmentManager().getBackStackEntryCount();
        if(bsEntryCount>=1) {
            mTransferMenuItem.setVisible(false);
        }else{
            mTransferMenuItem.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                pressBack();
                return true;
            case R.id.action_transfer:
                actionTransfer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pressBack(){
        int bsEntryCount = getFragmentManager().getBackStackEntryCount();
        if(bsEntryCount>=1) {
            String currentFragment = getFragmentManager()
                    .getBackStackEntryAt(bsEntryCount - 1)
                    .getName();

            Fragment fragment = getFragmentManager().findFragmentByTag(currentFragment);
            getFragmentManager().beginTransaction().remove(fragment).commit();
            getFragmentManager().popBackStack();
            layoutTansfer.setVisibility(View.VISIBLE);
            ((View)layoutTansfer.getParent()).findViewById(R.id.frame_content).setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != RESULT_OK)
            return;

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (requestCode == Application.ACTIVITY_LOOKUP) {
//            getIntent().putExtra(Application.EXTRA_IS_CONTACT,false);
//            identityConatct = (IdentityContact) intent.getSerializableExtra(Application.IDENTITY_LOOKUP);
//            if (identityConatct.getPublicKey().matches(PUBLIC_KEY_REGEX)) {
//                mReceiverPublicKey.setText(identityConatct.getPublicKey());
//                mContact.setText(identityConatct.getUid());
//            } else {
//                mReceiverPublicKey.setText("");
//            }
//        } else {
//            if (scanResult.getContents().matches(PUBLIC_KEY_REGEX)) {
//                mContact.setText("Find by Qr Code");
//                Map<String, String> data = Format.parseUri(scanResult.getContents());
//
//                String uid = Format.isNull(data.get(Format.UID));
//                String publicKey = Format.isNull(data.get(Format.PUBLICKEY));
//                String currencyName = Format.isNull(data.get(Format.CURRENCY));
//
//                mReceiverPublicKey.setText(publicKey);
//                if(uid.isEmpty()) {
//                    FindIdentityTask findIdentityTask = new FindIdentityTask(this, mcurrencyId, publicKey, this);
//                    findIdentityTask.execute();
//                }else{
//                    mContact.setText(uid);
//                }
//            } else
//                mReceiverPublicKey.setText("");
//        }
    }

    public void showDialog(){
        DialogFragment dialog= null;
        if(walletSelected!=null){
            dialog = ConverterDialog.newInstance( dividend,dt, amount, spinnerUnit,currency.getName());
        }else{
            Toast.makeText(this,getString(R.string.select_wallet),Toast.LENGTH_SHORT).show();
        }
        if(dialog!=null){
            dialog.show(getFragmentManager(), "listDialog");
        }
    }

    public void actionAfterWalletSelected(){
        if(walletSelected == null){
            dataWallet.setVisibility(View.GONE);
            noDataWallet.setVisibility(View.VISIBLE);
        }else {
            dataWallet.setVisibility(View.VISIBLE);
            noDataWallet.setVisibility(View.GONE);

            Format.Currency.changeUnit(
                    this,
                    currency.getName(),
                    walletSelected.getAmount(),
                    currency.getLastUdBlock().getDividend(),
                    currency.getDt().intValue(),
                    mWalletAmount,
                    mWalletDefaultAmount, "");

            mWalletAlias.setText(walletSelected.getAlias());

            setTitle(getResources().getString(R.string.transfer_of,currency.getName()));
        }

        if (contactSelected!=null){
            if (contactSelected.isContact()){
                mContact.setText(contactSelected.getUid());
                mReceiverPublicKey.setText(contactSelected.getPublicKey());
            }
        }
    }

    @Override
    public void onBackPressed() {
        pressBack();
    }

    public void send(Contact entity,String message) {
        if(entity==null){
            mContact.setText(message);
        }else {
            mContact.setText(entity.getUid());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.wallet:
                actionWallet();
                break;
            case R.id.layout_contact:
                mReceiverPublicKey.requestFocus();
                break;
            case R.id.layout_amount:
                amount.requestFocus();
                break;
            case R.id.action_calcul:
                actionAmount();
                break;
            case R.id.action_lookup:
                actionLookup();
                break;
            case R.id.action_scan_qrcode:
                actionScanQrCode();
                break;
        }
    }

    public void setContactSelected(Contact contact){

        if (layoutTansfer.getVisibility() == View.GONE) {
            ((View) layoutTansfer.getParent()).findViewById(R.id.frame_content).setVisibility(View.GONE);
            layoutTansfer.setVisibility(View.VISIBLE);
        }

        if (contact==null){
            return;
        }
        contactSelected = contact;
        mReceiverPublicKey.setText(contactSelected.getPublicKey());
        mReceiverPublicKey.requestFocus();
        mContact.setText(contactSelected.getUid());

        if (currency!=null && currency.getName()!=null) {
            setTitle(getResources().getString(R.string.transfer_of, currency.getName()));
        }
    }

    public void setWalletSelected(long walletId){
        if (layoutTansfer.getVisibility() == View.GONE) {
            ((View) layoutTansfer.getParent()).findViewById(R.id.frame_content).setVisibility(View.GONE);
            layoutTansfer.setVisibility(View.VISIBLE);
        }

        if(walletId!=-1){
            walletSelected = SqlService.getWalletSql(this).getById(walletId);
        }

        if (currency == null){
            currency = SqlService.getCurrencySql(this).getById(
                    contactSelected == null ?
                            walletSelected.getCurrency().getId() :
                            contactSelected.getCurrency().getId()
            );
            currency.setLastUdBlock(SqlService.getBlockSql(this).last(currency.getId()));
            if (walletSelected!=null){
                walletSelected.setCurrency(currency);
            }
            if (contactSelected!=null){
                contactSelected.setCurrency(currency);
            }

            dividend = currency.getLastUdBlock().getDividend();
            dt = currency.getDt().intValue();
        }

        actionAfterWalletSelected();
    }

    public void actionWallet(){
        layoutTansfer.setVisibility(View.GONE);
        ((View)layoutTansfer.getParent()).findViewById(R.id.frame_content).setVisibility(View.VISIBLE);

        Fragment fragment = WalletListFragment.newInstance(currency,false);
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public void actionAmount(){
        showDialog();
    }

    public void actionLookup() {
        layoutTansfer.setVisibility(View.GONE);
        ((View)layoutTansfer.getParent()).findViewById(R.id.frame_content).setVisibility(View.VISIBLE);

        Fragment fragment;

        if(mReceiverPublicKey.getText().toString().isEmpty()){
            fragment = IdentityListFragment.newInstance(currency.getId(), true, false,"");
        }else{
            String search ="";
            if (mReceiverPublicKey.getText().toString().length()>5){
                search = mContact.getText().toString();
            }else{
                search = mReceiverPublicKey.getText().toString();
            }
            fragment = IdentityListFragment.newInstance(currency.getId(), true, false,search);
        }
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public void actionScanQrCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        integrator.initiateScan();

    }

    public boolean actionTransfer() {
        final BigInteger qtAmount;
        String receiverPublicKey = mReceiverPublicKey.getText().toString();
        String comment;

        if ((receiverPublicKey = validatePublicKey(receiverPublicKey)) == null) return false;
        if ((qtAmount = validateAmount()) == null) return false;
        if ((comment = validateComment()) == null) return false;

        final String pubKey = receiverPublicKey;
        final String uid = contactSelected!=null ? contactSelected.getUid() : "";
        final String com = comment;

        //check funds
        if (qtAmount.compareTo(walletSelected.getAmount()) == 1) {
            defaultAmount.setError(getResources().getString(R.string.insufficient_funds));
            return false;
        }
        if (currency==null){
            return false;
        }

        TxDoc txDoc = new TxDoc(currency, comment);
        txDoc.addIssuer(walletSelected.getPublicKey());

        //set inputs
        BigInteger cumulativeAmount = BigInteger.ZERO;
        SourceSql sourceSql = SqlService.getSourceSql(this);
        List<Source> sources = sourceSql.getByWallet(walletSelected.getId());
        List<Source> usedSources = new ArrayList<>();
        for (Source source : sources) {
            txDoc.addInput(source.getType(),source.getIdentifier(),source.getNoffset());
            usedSources.add(source);
            cumulativeAmount = cumulativeAmount.add(source.getAmount());
            if (cumulativeAmount.compareTo(qtAmount) >= 0) {
                break;
            }
        }

        for (int i=0; i<usedSources.size(); i++) {
            txDoc.addUnlock(i,"0");
            sourceSql.delete(usedSources.get(i).getId());
        }

        txDoc.addOutput(qtAmount,0,receiverPublicKey);
        BigInteger refundAmount = cumulativeAmount.subtract(qtAmount);
        if (refundAmount.compareTo(BigInteger.ZERO) > 0) {
            txDoc.addOutput(refundAmount,0,walletSelected.getPublicKey());
        }

        boolean signed = false;
        //todo prompt for password
        try {
            signed = txDoc.sign(walletSelected.getPrivateKey());
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }
        if (signed) {
            final Context context = this;
            WalletService.payed(this, currency, txDoc, new WebService.WebServiceInterface() {
                @Override
                public void getDataFinished(int code, String response) {
                    if (code ==200){
                        JSONObject json = null;
                        String hash = "";
                        try {
                            json = new JSONObject(response);
                            hash = json.getString("hash");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Tx tx = new Tx();
                        BigInteger amount = BigInteger.ZERO;
                        amount = amount.subtract(qtAmount);
                        tx.setPublicKey(pubKey);
                        tx.setUid(uid);
                        tx.setAmount(amount);
                        tx.setCurrency(currency);
                        tx.setWallet(walletSelected);
                        tx.setState(TxState.PENDING.name());
                        tx.setComment(com);
                        tx.setEnc(com.length()>3 && com.substring(0,3).equals("ENC"));
                        tx.setTime(Long.valueOf("999999999999"));
                        tx.setHash(hash);
                        tx.setBlockNumber(0);
                        tx.setLocktime(0);

                        SqlService.getTxSql(context).insert(tx);

                        walletSelected.setAmount(walletSelected.getAmount().subtract(qtAmount));
                        SqlService.getWalletSql(context).update(walletSelected,walletSelected.getId());
                        finish();
                    }
                }
            });
        }
        return true;
    }

    private String validatePublicKey(String publicKey) {
        if (!publicKey.matches(PUBLIC_KEY_REGEX)) {
            mReceiverPublicKey.setError(getResources().getString(R.string.public_key_is_not_valid));
            mReceiverPublicKey.requestFocus();
            return null;
        }else{
            mReceiverPublicKey.setError(null);
        }
        return publicKey;
    }

    private BigInteger validateAmount() {
        if (amount.getText().toString().isEmpty()) {
            amount.setError(getResources().getString(R.string.amount_is_empty));
            amount.requestFocus();
            return null;
        }else{
            amount.setError(null);
        }
        return toQuantitative(new BigDecimal(amount.getText().toString()));
    }

    private String validateComment() {
//        if (!comment.matches(COMMENT_REGEX)) {
//            mComment.setError(getResources().getString(R.string.comment_is_not_valid));
//            return null;
//        }

        return mComment.getText().toString();
    }
}
