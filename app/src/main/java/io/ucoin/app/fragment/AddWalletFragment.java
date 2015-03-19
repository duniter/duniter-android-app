package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.CurrencyArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.crypto.KeyPair;

/**
 * A screen used to add a wallet via currency, uid, salt and password.
 */
public class AddWalletFragment extends Fragment {

    public static final String TAG = "AddWalletFragment";

    public static AddWalletFragment newInstance() {
        AddWalletFragment fragment = new AddWalletFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    // UI references.
    private CurrencyArrayAdapter mCurrencyAdapter;
    private Spinner mCurrencySpinner;
    private TextView mNameView;
    private TextView mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private ProgressViewAdapter mProgressViewAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Create the currencies adapter
        final List<Currency> currencies = ServiceLocator.instance().getCurrencyService().getCurrencies(getActivity());
        mCurrencyAdapter = new CurrencyArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_item,
                currencies
        );
        mCurrencyAdapter.setDropDownViewResource(CurrencyArrayAdapter.DEFAULT_LAYOUT_RES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_add_wallet,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Getting a given wallet
        Bundle newInstanceArgs = getArguments();
        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency.class.getSimpleName());

        // currency
        mCurrencySpinner = (Spinner) view.findViewById(R.id.currency);
        mCurrencySpinner.setAdapter(mCurrencyAdapter);

        // Name
        mNameView = (TextView) view.findViewById(R.id.name);
        mNameView.requestFocus();

        // user ID
        mUidView = (TextView) view.findViewById(R.id.uid);
        mUidView.requestFocus();

        // Salt
        mSaltView = (EditText) view.findViewById(R.id.salt);

        // Password
        mPasswordView = (EditText) view.findViewById(R.id.password);

        // Confirm password
        mConfirmPasswordView = (EditText) view.findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptAddWallet();
                    return true;
                }
                return false;
            }
        });

        Button mAddButton = (Button) view.findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddWallet();
            }
        });

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                mAddButton);

        // fill the UI with the given wallet
        updateView(currency);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.add_wallet);
        ((MainActivity) getActivity()).setBackButtonEnabled(true);
    }

    private void updateView(Currency currency) {
        if (currency != null) {
            int position = mCurrencyAdapter.getPosition(currency);
            mCurrencySpinner.setSelection(position);
        }
    }

    /**
     * Check if the form is valid, and launch the wallet creation.
     * If there are form errors (invalid uid, missing fields, etc.), the
     * errors are presented and no wallet will be created.
     */
    public void attemptAddWallet() {

        // Reset errors.
        mCurrencyAdapter.setError(mCurrencySpinner, null);
        mUidView.setError(null);
        mSaltView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        Currency currency = (Currency)mCurrencySpinner.getSelectedItem();
        String name = mNameView.getText().toString();
        String uid = mUidView.getText().toString();
        String salt = mSaltView.getText().toString();
        String password = mPasswordView.getText().toString();


        boolean cancel = false;
        View focusView = null;


        // Check for a valid currency
        if (currency == null) {
            mCurrencyAdapter.setError(mCurrencySpinner, getString(R.string.field_required));
            if (focusView == null) focusView = mCurrencySpinner;
            cancel = true;
        }

        // Check for a valid name (mandatory if uid is not set)
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(uid)) {
            mNameView.setError(getString(R.string.name_or_uid_required));
            if (focusView == null) focusView = mNameView;
            cancel = true;
        }

        // Check for a valid uid
        if (!TextUtils.isEmpty(uid) && !isUidValid(uid)) {
            mUidView.setError(getString(R.string.login_too_short));
            if (focusView == null) focusView = mUidView;
            cancel = true;
        }


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.password_too_short));
            if (focusView == null) focusView = mPasswordView;
            cancel = true;
        }
        // Check if password match
        else if (!ObjectUtils.equals(
                password,
                mConfirmPasswordView.getText().toString())) {
            mConfirmPasswordView.setError(getString(R.string.passwords_dont_match));
            if (focusView == null) focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check for a valid salt
        if (TextUtils.isEmpty(salt)) {
            mSaltView.setError(getString(R.string.field_required));
            if (focusView == null) focusView = mSaltView;
            cancel = true;
        } else if (!isSaltValid(salt)) {
            mSaltView.setError(getString(R.string.salt_too_short));
            if (focusView == null) focusView = mSaltView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Will show the progress bar, and create the wallet
            AddWalletTask task = new AddWalletTask();
            task.execute(currency, name, uid, salt, password);
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddWalletTask extends AsyncTaskHandleException<Object, Void, Wallet> {

        @Override
        protected void onPreExecute() {
            ViewUtils.hideKeyboard(getActivity());

            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Wallet doInBackgroundHandleException(Object... args) throws Exception {
            ObjectUtils.checkNotNull(args);
            ObjectUtils.checkArgument(args.length == 5);

            Currency currency = (Currency)args[0];
            String name = (String)args[1];
            String uid = (String)args[2];
            String salt = (String)args[3];
            String password = (String)args[4];

            // Compute a name is not set
            if (StringUtils.isBlank(name)) {
                name = uid;
            }

            String accountId = ((io.ucoin.app.Application) getActivity().getApplication()).getAccountId();

            // Create a seed from salt and password
            KeyPair keyPair = ServiceLocator.instance().getCryptoService().getKeyPair(salt, password);

            // Create a new wallet
            Wallet wallet = new Wallet(currency.getCurrencyName(), uid, keyPair.publicKey, keyPair.secretKey);
            wallet.setCurrencyId(currency.getId());
            wallet.setSalt(salt);
            wallet.setAccountId(Long.parseLong(accountId));
            wallet.setName(name);

            // Load membership
            ServiceLocator.instance().getBlockchainRemoteService().loadMembership(currency.getId(), wallet.getIdentity(), true);

            // Get credit
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();
            Long credit = txService.getCredit(currency.getId(), wallet.getPubKeyHash());
            wallet.setCredit(credit != null ? credit.intValue() : 0);

            // Save the wallet in DB
            // (reset private key first)
            wallet.setSecKey(null);
            ServiceLocator.instance().getWalletService().save(getActivity(), wallet);

            return wallet;
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            mProgressViewAdapter.showProgress(false);
            // Go back
            getFragmentManager().popBackStack();
        }

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);

            if (t instanceof DuplicatePubkeyException) {
                mUidView.setError(getString(R.string.duplicate_wallet_pubkey));
                mUidView.requestFocus();
            }
            else {
                Log.d(TAG, "Error in AddWalletTask", t);
                Toast.makeText(getActivity(),
                        t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }
}



