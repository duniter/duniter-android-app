package io.ucoin.app.activity;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.enumeration.SourceState;
import io.ucoin.app.enumeration.TxDirection;
import io.ucoin.app.fragment.currency.ContactListFragment;
import io.ucoin.app.fragment.currency.WalletListFragment;
import io.ucoin.app.fragment.dialog.ConverterDialog;
import io.ucoin.app.fragment.dialog.ListTransferDialog;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinContact;
import io.ucoin.app.model.UcoinContacts;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinSource;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.UcoinWallets;
import io.ucoin.app.model.document.Transaction;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.model.sql.sqlite.Contacts;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.model.sql.sqlite.Wallets;
import io.ucoin.app.Format;
import io.ucoin.app.task.FindIdentityTask;
import io.ucoin.app.task.FindIdentityTask.SendIdentity;
import io.ucoin.app.technical.crypto.AddressFormatException;

import static io.ucoin.app.fragment.currency.WalletListFragment.Action;

public class TransferActivity extends ActionBarActivity implements SendIdentity,Action {

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
    private EditText mReceiverPublicKey;
    private TextView defaultAmount;
    private EditText amount;
    private EditText mComment;
    private MenuItem mTransferMenuItem;
    private Long mcurrencyId;
    private Spinner spinnerUnit;
    private int unit;
    private int defaultUnit;
    private Currency currency;
    private LinearLayout layoutTansfer;
    private LinearLayout dataWallet;
    private TextView noDataWallet;
    private IdentityContact identityConatct;

    private UcoinWallet walletSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_transfer);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        unit = Integer.parseInt(preferences.getString(Application.UNIT, Application.UNIT_CLASSIC + ""));
        defaultUnit = Integer.parseInt(preferences.getString(Application.UNIT_DEFAULT, Application.UNIT_CLASSIC + ""));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mcurrencyId = getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID);
        identityConatct = (IdentityContact) getIntent().getExtras().getSerializable(Application.EXTRA_IDENTITY);



        try {
            setSupportActionBar(toolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout mWallet = (LinearLayout) findViewById(R.id.wallet);
        mWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionWallet();
            }
        });

        RelativeLayout layoutContact = (RelativeLayout) findViewById(R.id.layout_contact);
        layoutContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceiverPublicKey.requestFocus();
            }
        });

        RelativeLayout layoutAmount = (RelativeLayout) findViewById(R.id.layout_amount);
        layoutAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amount.requestFocus();
            }
        });

        layoutTansfer = (LinearLayout) findViewById(R.id.layout_transfer);
        dataWallet = (LinearLayout) findViewById(R.id.data_wallet);
        noDataWallet = (TextView) findViewById(R.id.no_data_wallet);

        mContact = (TextView) findViewById(R.id.contact);

        ImageButton mCalculate = (ImageButton) findViewById(R.id.action_calcul);
        mCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionAmount();
            }
        });

        ImageButton mSearchButton = (ImageButton) findViewById(R.id.action_lookup);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionLookup();
            }
        });

        ImageButton scanQrCode = (ImageButton) findViewById(R.id.action_scan_qrcode);
        scanQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionScanQrCode();
            }
        });

        mWalletAlias = (TextView) findViewById(R.id.wallet_alias);
        mWalletAmount = (TextView) findViewById(R.id.wallet_amount);
        mWalletDefaultAmount = (TextView) findViewById(R.id.wallet_default_amount);

        mReceiverPublicKey = (EditText) findViewById(R.id.receiver_public_key);

        amount = (EditText) findViewById(R.id.amount);
        defaultAmount = (TextView) findViewById(R.id.second_amount);
        amount.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Long walletId = getIntent().getLongExtra(Application.EXTRA_WALLET_ID,-1);
                if(walletId.equals(Long.valueOf(-1))){
                    dataWallet.requestFocus();
                }else {
                    majDefaultAmount();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        spinnerUnit = (Spinner) findViewById(R.id.spinner_unit);
        if(unit==Application.UNIT_TIME){
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

        mComment = (EditText) findViewById(R.id.comment);

        if (identityConatct != null) {
            mReceiverPublicKey.setText(identityConatct.getPublicKey());
            mContact.setText(identityConatct.getUid());
        }

        actionAfterWalletSelected();
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
                    quantitative = Format.Currency.relativeToQuantitative(this, new BigDecimal(val), walletSelected.udValue());
                    break;
                case Application.UNIT_TIME:
                    val = Format.Time.toSecond(this, new BigDecimal(val), spinnerUnit.getSelectedItemPosition()).toString();
                    quantitative = Format.Currency.timeToQuantitative(this, new BigDecimal(val), walletSelected.currency().dt(), walletSelected.udValue());
                    break;
            }
            Format.Currency.changeUnit(this, walletSelected.currency().name(), quantitative, walletSelected.udValue(), walletSelected.currency().dt(), null, defaultAmount, "");
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
                    res = Format.Currency.relativeToQuantitative(this, val, walletSelected.udValue());
                    break;
                case Application.UNIT_TIME:
                    val = Format.Time.toSecond(this, val, spinnerUnit.getSelectedItemPosition());
                    res = Format.Currency.timeToQuantitative(this, val, walletSelected.currency().dt(), walletSelected.udValue());
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
        if (requestCode == Application.ACTIVITY_LOOKUP) {
            getIntent().putExtra(Application.EXTRA_IS_CONTACT,false);
            identityConatct = (IdentityContact) intent.getSerializableExtra(Application.IDENTITY_LOOKUP);
            if (identityConatct.getPublicKey().matches(PUBLIC_KEY_REGEX)) {
                mReceiverPublicKey.setText(identityConatct.getPublicKey());
                mContact.setText(identityConatct.getUid());
            } else {
                mReceiverPublicKey.setText("");
            }
        } else {
            if (scanResult.getContents().matches(PUBLIC_KEY_REGEX)) {
                mContact.setText("Find by Qr Code");
                Map<String, String> data = Format.parseUri(scanResult.getContents());

                String uid = Format.isNull(data.get(Format.UID));
                String publicKey = Format.isNull(data.get(Format.PUBLICKEY));
                String currencyName = Format.isNull(data.get(Format.CURRENCY));

                mReceiverPublicKey.setText(publicKey);
                if(uid.isEmpty()) {
                    FindIdentityTask findIdentityTask = new FindIdentityTask(this, mcurrencyId, publicKey, this);
                    findIdentityTask.execute();
                }else{
                    mContact.setText(uid);
                }
            } else
                mReceiverPublicKey.setText("");
        }
    }

    public void showDialog(int type,Cursor list){
        DialogFragment dialog= null;
        Long walletId = getIntent().getLongExtra(Application.EXTRA_WALLET_ID,-1);
        if(walletSelected!=null && !walletId.equals(Long.valueOf(-1))){
            dialog = new ConverterDialog(
                    walletSelected.udValue(),
                    currency.dt(), amount, spinnerUnit,walletSelected.currency().name());
        }
        if(dialog!=null){
            dialog.show(getFragmentManager(), "listDialog");
        }
    }

    public void actionAfterWalletSelected(){
        Long walletId = getIntent().getLongExtra(Application.EXTRA_WALLET_ID,-1);
        if(walletId.equals(Long.valueOf(-1))){
            dataWallet.setVisibility(View.GONE);
            noDataWallet.setVisibility(View.VISIBLE);
        }else {
            dataWallet.setVisibility(View.VISIBLE);
            noDataWallet.setVisibility(View.GONE);
            UcoinWallets wallets = new Wallets(Application.getContext(), mcurrencyId);
            walletSelected = wallets.getById(walletId);

            Format.Currency.changeUnit(this, walletSelected.currency().name(), walletSelected.quantitativeAmount(), walletSelected.udValue(), walletSelected.currency().dt(), mWalletAmount, mWalletDefaultAmount, "");

            mWalletAlias.setText(walletSelected.alias());

            setTitle(getResources().getString(R.string.transfer_of,walletSelected.currency().name()));
            currency = new Currency(this, walletSelected.currencyId());
        }
        //mQuantitativeUD = new BigDecimal(data.getString(data.getColumnIndex(SQLiteView.Wallet.UD_VALUE)));

        //TODO fma metre les valeurs des amount a jour

        boolean isContact = getIntent().getExtras().getBoolean(Application.EXTRA_IS_CONTACT);

        if(isContact){
            Long contactId = getIntent().getExtras().getLong(Application.EXTRA_CONTACT_ID);
            UcoinContacts contacts = new Contacts(Application.getContext(),mcurrencyId);
            UcoinContact contact = contacts.getById(contactId);
            mContact.setText(contact.name());
            mReceiverPublicKey.setText(contact.publicKey());
        }
    }

    @Override
    public void onBackPressed() {
        pressBack();
    }

    @Override
    public void send(IdentityContact entity,String message) {
        if(entity==null){
            mContact.setText(message);
        }else {
            mContact.setText(entity.getUid());
        }
    }

    @Override
    public void displayWalletFragment(Long walletId) {
        pressBack();
        getIntent().putExtra(Application.EXTRA_WALLET_ID, walletId);
        actionAfterWalletSelected();
    }

    public interface DialogItemClickListener{
        void onClick(Long id);
    }

    public void actionWallet(){
        layoutTansfer.setVisibility(View.GONE);
        ((View)layoutTansfer.getParent()).findViewById(R.id.frame_content).setVisibility(View.VISIBLE);

        Fragment fragment = WalletListFragment.newInstance(mcurrencyId,false);
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
        showDialog(ListTransferDialog.TYPE_CURRENCY, (Cursor) null);
    }

    public void actionLookup() {
        Intent intent = new Intent(this, LookupActivity.class);
        Long currencyId = currency.id();
        intent.putExtra(Application.EXTRA_CURRENCY_ID, currencyId);
        intent.putExtra(ContactListFragment.SEE_CONTACT,true);
        intent.putExtra(ContactListFragment.ADD_CONTACT,false);
        intent.putExtra(ContactListFragment.OPEN_SEARCH,true);
        intent.putExtra(ContactListFragment.TEXT_SEARCH,mReceiverPublicKey.getText().toString());
        intent.putExtra(SEARCH_IDENTITY, mReceiverPublicKey.getText().toString());
        startActivityForResult(intent, Application.ACTIVITY_LOOKUP);
    }

    public void actionScanQrCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        integrator.initiateScan();

    }

    public boolean actionTransfer() {
        BigInteger qtAmount;
        String receiverPublicKey = mReceiverPublicKey.getText().toString();
        String comment;

        if ((receiverPublicKey = validatePublicKey(receiverPublicKey)) == null) return false;
        if ((qtAmount = validateAmount()) == null) return false;
        if ((comment = validateComment()) == null) return false;

        final UcoinWallet wallet = walletSelected;

        //Create Tx
        final Transaction transaction = new Transaction();
        transaction.setCurrency(wallet.currency().name());
        transaction.setComment(comment);
        transaction.addIssuer(wallet.publicKey());


        //check funds
        if (qtAmount.compareTo(wallet.quantitativeAmount()) == 1) {
            defaultAmount.setError(getResources().getString(R.string.insufficient_funds));
            return false;
        }

        //set inputs
        BigInteger cumulativeAmount = new BigInteger("0");
        for (UcoinSource source : wallet.sources().getByState(SourceState.AVAILABLE)) {
            transaction.addInput(source);
            source.setState(SourceState.CONSUMED);
            cumulativeAmount.add(source.amount());
            if (cumulativeAmount.compareTo(qtAmount) == 1) {
                break;
            }
        }

        // set outputs
        // a public address can appear juste once, hence if we send from a wallet to itself,
        // we send the total amount
        if (receiverPublicKey.equals(wallet.publicKey())) {
            transaction.addOuput(receiverPublicKey, cumulativeAmount);
        } else {
            transaction.addOuput(receiverPublicKey, qtAmount);
            BigInteger refundAmount = cumulativeAmount.subtract(qtAmount);
            if (refundAmount.compareTo(BigInteger.ZERO) > 0) {
                transaction.addOuput(wallet.publicKey(), refundAmount);
            }
        }

        //todo prompt for password
        try {
            transaction.addSignature(transaction.sign(wallet.privateKey()));
        } catch (AddressFormatException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        UcoinEndpoint endpoint = wallet.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/tx/process/";

        mTransferMenuItem.setEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_send_grey600_24dp, null);
        mTransferMenuItem.setIcon(drawable);
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        TxHistory.PendingTx tx = TxHistory.PendingTx.fromJson(response);
                        wallet.txs().add(tx, TxDirection.OUT);
                        Toast.makeText(TransferActivity.this, getResources().getString(R.string.transaction_sent), Toast.LENGTH_LONG).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTransferMenuItem.setEnabled(true);
                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_send_white_24dp, null);
                        mTransferMenuItem.setIcon(drawable);

                        for(UcoinSource source : transaction.getSources()) {
                            source.setState(SourceState.AVAILABLE);
                        }

                        if (error instanceof NoConnectionError) {
                            Toast.makeText(Application.getContext(),
                                    getResources().getString(R.string.no_connection),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
                            Log.d("TRANSFERACTIVITY", new String(error.networkResponse.data));
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("transaction", transaction.toString());
                return params;
            }
        };
        request.setTag(this);
        Application.getRequestQueue().add(request);

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
