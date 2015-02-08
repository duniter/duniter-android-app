package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.WotLookupUId;
import io.ucoin.app.service.DataContext;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.crypto.KeyPair;

/**
 * A login screen that offers login via email/password.
 */
public class LoginFragment extends Fragment {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private ProgressViewAdapter mProgressViewAdapter;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_login,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.sign_in));

        // UID
        mUidView = (EditText) view.findViewById(R.id.uid);

        // Salt
        mSaltView = (EditText) view.findViewById(R.id.salt);

        // Password
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.uid || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) view.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.login_progress),
                mSignInButton);

        // fill the UI with the default wallet
        Wallet defaultWallet = ServiceLocator.instance().getWalletService().getDefaultWallet(getActivity().getApplication());
        updateView(defaultWallet);
    }

    private void updateView(Wallet wallet) {
        String uid = null;
        String salt = null;
        if (wallet != null) {
            if (wallet.getIdentity() != null) {
                uid = wallet.getIdentity().getUid();
            }
            if (wallet.getSalt() != null) {
                salt = wallet.getSalt();
            }
        }
        mUidView.setText(uid);
        mSaltView.setText(salt);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUidView.setError(null);
        mSaltView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String uid = mUidView.getText().toString();
        String email = mSaltView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid uid
        if (TextUtils.isEmpty(uid)) {
            mUidView.setError(getString(R.string.field_required));
            focusView = mUidView;
            cancel = true;
        } else if (!isUidValid(password)) {
            mUidView.setError(getString(R.string.login_too_short));
            focusView = mUidView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.password_too_short));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mSaltView.setError(getString(R.string.field_required));
            focusView = mSaltView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mSaltView.setError(getString(R.string.email_address_invalid));
            focusView = mSaltView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mAuthTask = new UserLoginTask(uid, email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUidValid(String uid) {
        return uid.length() >= 4;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    // TODO kimamila: use a loader to load existing Wallet, and then use this list to check is salt+password is correct
    /*@Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), WalletQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> walletPubkeys = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            walletPubkeys.add(cursor.getString(WalletQuery.PUBLIC_KEY));
            cursor.moveToNext();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface WalletQuery {
        String[] PROJECTION = {
                Contract.Wallet._ID,
                Contract.Wallet.PUBLIC_KEY,
        };

        int ID = 0;
        int PUBLIC_KEY = 1;
    }
    */

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTaskHandleException<Void, Void, Boolean> {

        private final String mUid;
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String uid, String email, String password) {
            mUid = uid;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Boolean doInBackgroundHandleException(Void... params) throws Exception {
            DataContext context = ServiceLocator.instance().getDataContext();
            String currency = "??";
            if (context.getBlockchainParameter() != null) {
                currency = context.getBlockchainParameter().getCurrency();
            }

            // Create a seed from salt and password
            KeyPair keyPair = ServiceLocator.instance().getCryptoService().getKeyPair(mEmail, mPassword);

            // Create the wallet
            Wallet wallet = new Wallet(currency, mUid, keyPair.publicKey, keyPair.secretKey);

            WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
            WotLookupUId result = wotService.findByUidAndPublicKey(mUid, wallet.getPubKeyHash());
            if (result != null) {

                // Refresh the wallet identity with lookup info
                wotService.toIdentity(result, wallet.getIdentity());

                // Store the wallet into the data context
                context.setWallet(wallet);

                // TODO : Check is the wallet exists on DB account's wallets

                return true;
            }

            // TODO: register the new account here.
            // send the self

            return false;
        }

        @Override
        protected void onSuccess(Boolean success) {
            mAuthTask = null;

            if (success) {
                Fragment fragment = HomeFragment.newInstance();
                getFragmentManager().popBackStack();
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.animator.fade_in,
                                R.animator.fade_out)
                        .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();
            } else {
                mUidView.setError(getString(R.string.login_incorrect));
                mUidView.requestFocus();
                mProgressViewAdapter.showProgress(false);
            }
        }

        @Override
        protected void onFailed(Throwable t) {
            mPasswordView.setError(t.getMessage());
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            mProgressViewAdapter.showProgress(false);
        }
    }
}



