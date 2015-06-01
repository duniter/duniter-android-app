package io.ucoin.app.fragment.common;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.local.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.crypto.KeyPair;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

/**
 * A login screen that offers login via salt and password.
 */
public class LoginFragment extends Fragment {


    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public static LoginFragment newInstance(Wallet wallet, OnLoginListener listener) {
        ObjectUtils.checkNotNull(wallet);
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Wallet.class.getSimpleName(), wallet);

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(newInstanceArgs);
        fragment.setOnLoginListener(listener);
        return fragment;
    }

    public static void login(final FragmentManager fragmentManager, final Wallet wallet, LoginFragment.OnLoginListener listener) {
        ObjectUtils.checkNotNull(wallet);
        ObjectUtils.checkNotNull(fragmentManager);

        if (wallet.isAuthenticate()) {
            listener.onSuccess(wallet);
            return;
        }

        LoginFragment fragment = LoginFragment.newInstance(wallet, listener);

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private OnLoginListener mListener;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private TextView mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private ProgressViewAdapter mProgressViewAdapter;


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

        // Getting a given wallet
        Bundle newInstanceArgs = getArguments();
        final Wallet wallet = (Wallet) newInstanceArgs
                .getSerializable(Wallet.class.getSimpleName());

        getActivity().setTitle(getString(R.string.sign_in));

        // UID
        mUidView = (TextView) view.findViewById(R.id.uid);

        // Salt
        mSaltView = (EditText) view.findViewById(R.id.salt);

        // Password
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin(wallet);
                    return true;
                }
                return false;
            }
        });
        mPasswordView.requestFocus();

        Button mSignInButton = (Button) view.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(wallet);
            }
        });

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                mSignInButton);

        // fill the UI with the given wallet
        updateView(wallet);
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
    public void attemptLogin(Wallet wallet) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUidView.setError(null);
        mSaltView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String uid = mUidView.getText().toString();
        String salt = mSaltView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid uid
        if (TextUtils.isEmpty(uid)) {
            mUidView.setError(getString(R.string.field_required));
            focusView = mUidView;
            cancel = true;
        } else if (!isUidValid(uid)) {
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

        // Check for a valid salt address.
        if (TextUtils.isEmpty(salt)) {
            mSaltView.setError(getString(R.string.field_required));
            focusView = mSaltView;
            cancel = true;
        } else if (!isSaltValid(salt)) {
            mSaltView.setError(getString(R.string.salt_too_short));
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
            mAuthTask = new UserLoginTask(salt, password);
            mAuthTask.execute(wallet);
        }
    }

    private boolean isUidValid(String uid) {
        // TODO : voir s'il y a une taille mininum
        return uid.length() >= 2;
    }

    private boolean isSaltValid(String salt) {
        return salt.length() >= 3;
    }

    private boolean isPasswordValid(String password) {
        // TODO : voir s'il y a une taille mininum
        return password.length() >= 3;
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
            walletPubkeys.add(cursor.getString(WalletQuery.ISSUERS));
            cursor.moveToNext();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface WalletQuery {
        String[] PROJECTION = {
                Contract.Wallet._ID,
                Contract.Wallet.ISSUERS,
        };

        int ID = 0;
        int ISSUERS = 1;
    }
    */

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTaskHandleException<Wallet, Void, Wallet> {

        private final String mSalt;
        private final String mPassword;

        UserLoginTask(String salt, String password) {
            super(getActivity());
            mSalt = salt;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getActivity().getCurrentFocus())
                            ? null
                            : getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            // SHow the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Wallet... wallets) throws Exception {
            Wallet wallet = wallets[0];

            // Create a seed from salt and password
            KeyPair keyPair = ServiceLocator.instance().getCryptoService().getKeyPair(mSalt, mPassword);

            // Make sure the password is correct
            if (!Arrays.equals(wallet.getPubKey(), keyPair.getPubKey())) {
                // wrong salt/password
                return null;
            }

            // Update the wallet
            wallet.setPubKey(keyPair.publicKey);
            wallet.setSecKey(keyPair.secretKey);

            return wallet;
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            mAuthTask = null;

            mProgressViewAdapter.showProgress(false);
            if (wallet != null) {
                mListener.onSuccess(wallet);
            } else {
                mPasswordView.setError(getString(R.string.password_incorrect));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onFailed(Throwable t) {

            mProgressViewAdapter.showProgress(false);
            Toast.makeText(getActivity(),
                    t.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            mProgressViewAdapter.showProgress(false);
        }
    }

    private void setOnLoginListener(OnLoginListener listener) {
        mListener = listener;
    }

    public interface OnLoginListener {
        public void onSuccess(Wallet authWallet);
    }
}



