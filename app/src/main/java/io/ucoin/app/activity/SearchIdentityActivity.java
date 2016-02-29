package io.ucoin.app.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currency;

/**
 * Created by naivalf27 on 07/12/15.
 */
public class SearchIdentityActivity extends ActionBarActivity {
    private Toolbar mToolbar;
    private EditText mName;
    private EditText mPublicKey;
    private EditText mUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_contact);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        ImageButton lookup = (ImageButton) findViewById(R.id.action_lookup);
        lookup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchIdentityActivity.this,
                        LookupActivity.class);
                intent.putExtra(Application.EXTRA_CURRENCY_ID, getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID));
                startActivityForResult(intent, Application.ACTIVITY_LOOKUP);
            }
        });

        ImageButton scanQrCode = (ImageButton) findViewById(R.id.action_scan_qrcode);
        scanQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(SearchIdentityActivity.this);
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


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode != RESULT_OK)
            return;

        if(requestCode == Application.ACTIVITY_LOOKUP) {
            WotLookup.Result result = (WotLookup.Result)intent.getExtras().getSerializable(WotLookup.Result.class.getSimpleName());
            mName.setText(result.uids[0].uid);
            mUid.setText(result.uids[0].uid);
            mPublicKey.setText(result.pubkey);
        } else {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null && !scanResult.getContents().isEmpty()) {
                mPublicKey.setText(scanResult.getContents());
            }
        }
    }

    public void actionAddContact() {
        String name = mName.getText().toString();
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        String uid = mUid.getText().toString();
        if (uid.isEmpty()) {
            Toast.makeText(this, "Uid is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        String publicKey = mPublicKey.getText().toString();
        if (publicKey.isEmpty()) {
            Toast.makeText(this, "public key is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        Long currencyId = getIntent().getExtras().getLong(Application.EXTRA_CURRENCY_ID);
        UcoinCurrency currency = new Currency(this, currencyId);
        currency.contacts().add(name, uid, publicKey);
        finish();
    }
}