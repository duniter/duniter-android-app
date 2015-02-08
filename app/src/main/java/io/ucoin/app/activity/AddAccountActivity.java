package io.ucoin.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.AccountService;
import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.crypto.Base58;
import io.ucoin.app.technical.crypto.KeyPair;

public class AddAccountActivity extends ActionBarActivity implements TextView.OnEditorActionListener  {

    private TextView mUidHint;
    private TextView mSaltHint;
    private TextView mPasswordHint;
    private EditText mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private ProgressViewAdapter mProgressViewAdapter;

    // TODO : to remove !
    private boolean isDev = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        mUidHint = (TextView) findViewById(R.id.uid_tip);
        mSaltHint = (TextView) findViewById(R.id.salt_tip);
        mPasswordHint = (TextView) findViewById(R.id.password_tip);

        mUidView = (EditText) findViewById(R.id.uid);
        mSaltView = (EditText) findViewById(R.id.salt);
        mPasswordView = (EditText) findViewById(R.id.password);

        mUidView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mUidHint.setVisibility(View.VISIBLE);
                } else {
                    mUidHint.setVisibility(View.GONE);
                }
            }
        });
        mSaltView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mSaltHint.setVisibility(View.VISIBLE);
                } else {
                    mSaltHint.setVisibility(View.GONE);
                }
            }
        });
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mPasswordHint.setVisibility(View.VISIBLE);
                } else {
                    mPasswordHint.setVisibility(View.GONE);
                }
            }
        });

        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnFocusChangeListener(mPasswordView.getOnFocusChangeListener());

        mConfirmPasswordView.setOnEditorActionListener(this);

        mProgressViewAdapter = new ProgressViewAdapter(
                this,
                R.id.load_progress,
                R.id.account_form
        );

        // TODO to remove
        if (isDev) {
            Wallet devWallet = ServiceLocator.instance().getWalletService().getDefaultWallet(getApplication());
            mUidView.setText(devWallet.getIdentity().getUid());
            mSaltView.setText(devWallet.getSalt());

        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_DONE) {
            return false;
        }

        //validate uid
        String uid = mUidView.getText().toString();
        if (uid.isEmpty()) {
            mUidView.setError(getString(R.string.uid_cannot_be_empty));
            return false;
        }

        //validate salt
        String salt = mSaltView.getText().toString();
        if (salt.isEmpty()) {
            mSaltView.setError(getString(R.string.salt_cannot_be_empty));
            return false;
        }

        //validate password
        String password = mPasswordView.getText().toString();
        if (password.isEmpty()) {
            mPasswordView.setError(getString(R.string.password_cannot_be_empty));
            return false;
        }

        String confirmPassword = mConfirmPasswordView.getText().toString();
        if (confirmPassword.isEmpty()) {
            mConfirmPasswordView.setError(getString(R.string.confirm_password_cannot_be_empty));
            return false;
        }

        if(!password.equals(confirmPassword)) {
            mPasswordView.setError(getString(R.string.passwords_dont_match));
            mConfirmPasswordView.setError(getString(R.string.passwords_dont_match));
            return false;
        }

        AddAccountTask addAccountTask = new AddAccountTask(uid, salt, password);
        addAccountTask.execute();

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddAccountTask extends AsyncTaskHandleException<Void, Void, Boolean> {

        private final String mUid;
        private final String mSalt;
        private final String mPassword;

        AddAccountTask(String uid, String salt, String password) {
            mUid = uid;
            mSalt = salt;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Boolean doInBackgroundHandleException(Void... params) throws Exception {
            //generate keys
            CryptoService service = ServiceLocator.instance().getCryptoService();
            KeyPair keys = service.getKeyPair(mSalt, mPassword);

            // Save into DB
            AccountService accountService = ServiceLocator.instance().getAccountService();
            io.ucoin.app.model.Account account = new io.ucoin.app.model.Account();
            account.setUid(mUid);
            account.setPubkey(Base58.encode(keys.getPubKey()));
            account.setSalt(mSalt);

            accountService.save(AddAccountActivity.this, account);
            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            //restart MainActivity
            Intent intent = new Intent(AddAccountActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        protected void onFailed(Throwable t) {
            mUidView.setError(t.getMessage());
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }
}