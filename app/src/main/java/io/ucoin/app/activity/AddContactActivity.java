package io.ucoin.app.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Map;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currencies;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.Format;
import io.ucoin.app.task.FindIdentityTask;
import io.ucoin.app.task.FindIdentityTask.SendIdentity;

public class AddContactActivity extends ActionBarActivity implements SendIdentity{
    private Toolbar mToolbar;
    private EditText mName;
    private EditText mUid;
    private EditText mPublicKey;
    private UcoinCurrency currency;

    private Long currencyId;

    public static final int CONTACT = 10003;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_contact);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        currencyId = getIntent().getLongExtra(Application.EXTRA_CURRENCY_ID,-1);



        LinearLayout layoutName = (LinearLayout) findViewById(R.id.layout_name);
        layoutName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mName.requestFocus();
            }
        });
        LinearLayout layoutUid = (LinearLayout) findViewById(R.id.layout_uid);
        layoutUid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUid.requestFocus();
            }
        });
        LinearLayout layoutPublickey = (LinearLayout) findViewById(R.id.layout_publickey);
        layoutPublickey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPublicKey.requestFocus();
            }
        });


        ImageButton lookup = (ImageButton) findViewById(R.id.action_lookup);
        lookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LookupActivity.class);
                intent.putExtra(Application.EXTRA_CURRENCY_ID, currencyId);
                startActivityForResult(intent, Application.ACTIVITY_LOOKUP);
            }
        });

        ImageButton scanQrCode = (ImageButton) findViewById(R.id.action_scan_qrcode);
        scanQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(AddContactActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                integrator.initiateScan();
            }
        });

        try {
            setSupportActionBar(mToolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mName = (EditText) findViewById(R.id.name);
        mUid = (EditText) findViewById(R.id.uid);
        mPublicKey = (EditText) findViewById(R.id.public_key);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbar.inflateMenu(R.menu.toolbar_add_contact);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_add_contact:
                actionAddContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void send(IdentityContact entity,String message) {
        if(entity!=null) {
            mUid.setText(entity.getUid());
            mName.setHint(entity.getUid());
            this.currency = new Currencies(this).getById(entity.getCurrencyId());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode != RESULT_OK)
            return;

        if(requestCode == Application.ACTIVITY_LOOKUP) {
            IdentityContact identityContact = (IdentityContact) intent.getSerializableExtra(Application.IDENTITY_LOOKUP);
            WotLookup.Result result = (WotLookup.Result)intent.getExtras().getSerializable(WotLookup.Result.class.getSimpleName());
            mUid.setText(identityContact.getUid());
            if(identityContact.getName().isEmpty()){
                mName.setHint(identityContact.getUid());
            }else {
                mName.setText(identityContact.getName());
            }
            mPublicKey.setText(identityContact.getPublicKey());
            currency = new Currency(this,identityContact.getCurrencyId());
        } else if(requestCode == CONTACT){
            finish();
            Toast.makeText(this,
                    getString(R.string.contact_added),
                    Toast.LENGTH_SHORT).show();
        } else{
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null && !scanResult.getContents().isEmpty()) {
                String result = scanResult.getContents();

                Map<String, String> data = Format.parseUri(result);

                String uid = Format.isNull(data.get(Format.UID));
                String publicKey = Format.isNull(data.get(Format.PUBLICKEY));
                String currencyName = Format.isNull(data.get(Format.CURRENCY));

                mUid.setText(uid);
                mName.setHint(uid);
                currency = new Currencies(this).getByName(currencyName);
                mPublicKey.setText(publicKey);

                if(uid.equals("")){
                    FindIdentityTask findIdentityTask = new FindIdentityTask(
                            this,
                            currencyId,
                            publicKey,
                            this);
                    findIdentityTask.execute();
                }
            }
        }
    }

    private void actionAddContact() {
        final String uid = mUid.getText().toString();
        final String name = mName.getText().toString();
        final String publicKey = mPublicKey.getText().toString();
        if (publicKey.isEmpty()) {
            Toast.makeText(this, "public key is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        if(uid.isEmpty() && name.isEmpty()){
            Toast.makeText(this, "name is invalid", Toast.LENGTH_SHORT).show();
            mName.requestFocus();
            return;
        }

        if(currency==null){
            Long currencyId = getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID);
            currency = new Currency(this, currencyId);
        }

        if (uid.isEmpty()){
            currency.contacts().add(name, "", publicKey);
            askContactInPhone();
        }else{
            currency.contacts().add(
                    (name.isEmpty()) ? uid : name,
                    uid,
                    publicKey);
            askContactInPhone();
        }
    }

    private void addNewContactInPhone(){
        String name = mName.getText().toString();
        String uid = mUid.getText().toString();
        String publicKey = mPublicKey.getText().toString();

        String url = Format.createUri(Format.LONG, uid, publicKey, currency.name());

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
        startActivityForResult(intent, CONTACT);
        //------------------------------- end of inserting contact in the phone
    }

    private void askContactInPhone(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Contact");
        alertDialogBuilder
                .setMessage("Do you want to save the contact on your phone ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addNewContactInPhone();
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}