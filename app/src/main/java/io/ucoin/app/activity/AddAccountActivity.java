package io.ucoin.app.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.crypto.Base58;
import io.ucoin.app.technical.crypto.KeyPair;

public class AddAccountActivity extends ActionBarActivity implements TextView.OnEditorActionListener  {

    private TextView mUidHint;
    private TextView mSaltHint;
    private TextView mPasswordHint;
    private EditText mUid;
    private EditText mSalt;
    private EditText mPassword;
    private EditText mConfirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        mUidHint = (TextView) findViewById(R.id.uid_tip);
        mSaltHint = (TextView) findViewById(R.id.salt_tip);
        mPasswordHint = (TextView) findViewById(R.id.password_tip);

        mUid = (EditText) findViewById(R.id.uid);
        mSalt = (EditText) findViewById(R.id.salt);
        mPassword = (EditText) findViewById(R.id.password);

        mUid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mUidHint.setVisibility(View.VISIBLE);
                } else {
                    mUidHint.setVisibility(View.GONE);
                }
            }
        });
        mSalt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mSaltHint.setVisibility(View.VISIBLE);
                } else {
                    mSaltHint.setVisibility(View.GONE);
                }
            }
        });
        mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mPasswordHint.setVisibility(View.VISIBLE);
                } else {
                    mPasswordHint.setVisibility(View.GONE);
                }
            }
        });

        mConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        mConfirmPassword.setOnFocusChangeListener(mPassword.getOnFocusChangeListener());

        mConfirmPassword.setOnEditorActionListener(this);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_DONE) {
            return false;
        }

        //todo validate inputs


        //validate uid
        String uid = mUid.getText().toString();
        if (uid.isEmpty()) {
            mUid.setError(getString(R.string.uid_cannot_be_empty));
            return false;
        }

        //validate salt
        String salt = mSalt.getText().toString();
        if (salt.isEmpty()) {
            mSalt.setError(getString(R.string.salt_cannot_be_empty));
            return false;
        }

        //validate password
        String password = mPassword.getText().toString();
        if (password.isEmpty()) {
            mPassword.setError(getString(R.string.password_cannot_be_empty));
            return false;
        }

        String confirmPassword = mConfirmPassword.getText().toString();
        if (confirmPassword.isEmpty()) {
            mConfirmPassword.setError(getString(R.string.confirm_password_cannot_be_empty));
            return false;
        }

        if(!password.equals(confirmPassword)) {
            mPassword.setError(getString(R.string.passwords_dont_match));
            mConfirmPassword.setError(getString(R.string.passwords_dont_match));
            return false;
        }

        //generate keys
        CryptoService service = ServiceLocator.instance().getCryptoService();
        KeyPair keys = service.getKeyPair(salt, password);

        //Create account in database
        ContentValues values = new ContentValues();
        values.put(Contract.Account.UID, uid);
        values.put(Contract.Account.PUBLIC_KEY, Base58.encode(keys.getPubKey()));

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        uri = getContentResolver().insert(uri, values);
        Long accountId = ContentUris.parseId(uri);

        //create account in android framework
        Bundle data = new Bundle();
        data.putString(Contract.Account._ID, accountId.toString());
        data.putString(Contract.Account.PUBLIC_KEY, Base58.encode(keys.getPubKey()));
        Account account = new Account(uid, getString(R.string.ACCOUNT_TYPE));
        AccountManager.get(this).addAccountExplicitly(account, null, data);

        //keep a reference to the last account used
        SharedPreferences.Editor editor =
                getSharedPreferences("account", Context.MODE_PRIVATE).edit();
        editor.putString("_id", Long.toString(ContentUris.parseId(uri)));
        editor.apply();

        //restart MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}