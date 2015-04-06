package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.CurrencyArrayAdapter;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.CurrencyService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.DuplicatePubkeyException;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.crypto.KeyPair;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

/**
 * A screen used to add a wallet via currency, uid, salt and password.
 */
public class AddWalletDialogFragment extends DialogFragment {

    public static final String TAG = "AddWalletDialog";

    public static AddWalletDialogFragment newInstance(Activity activity) {
        ObjectUtils.checkNotNull(activity);

        // If only ONE currency in database, force to select this one
        CurrencyService currencyService = ServiceLocator.instance().getCurrencyService();
        int currencyCount = currencyService.getCurrencyCount();
        if (currencyCount == 1) {
            return newInstance(currencyService.getCurrencies(activity).iterator().next());
        }

        AddWalletDialogFragment fragment = new AddWalletDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public static AddWalletDialogFragment newInstance(Currency currency) {
        ObjectUtils.checkNotNull(currency);

        AddWalletDialogFragment fragment = new AddWalletDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(Currency.class.getSimpleName(), currency);
        fragment.setArguments(args);
        return fragment;
    }

    // UI references.
    private CurrencyArrayAdapter mCurrencyAdapter;
    private Spinner mCurrencySpinner;
    private TextView mAliasView;
    private TextView mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_wallet_dialog, null);
        builder.setView(view);
        builder.setTitle(R.string.add_wallet_title);

        Bundle newInstanceArgs = getArguments();
        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency.class.getSimpleName());

        // Currency
        mCurrencySpinner = (Spinner) view.findViewById(R.id.currency);
        if (currency != null) {
            view.findViewById(R.id.currency_label).setVisibility(View.GONE);
            mCurrencySpinner.setVisibility(View.GONE);
            mCurrencyAdapter = new CurrencyArrayAdapter(
                    getActivity(),
                    android.R.layout.simple_spinner_item);
        }
        else {
            // Create the currencies adapter
            final List<Currency> currencies = ServiceLocator.instance().getCurrencyService().getCurrencies(getActivity());
            mCurrencyAdapter = new CurrencyArrayAdapter(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    currencies);
            mCurrencyAdapter.setDropDownViewResource(CurrencyArrayAdapter.DEFAULT_LAYOUT_RES);
        }
        mCurrencySpinner.setAdapter(mCurrencyAdapter);

        // Alias
        mAliasView = (TextView) view.findViewById(R.id.alias);
        mAliasView.requestFocus();

        // UID
        final TextView mUidTip = (TextView) view.findViewById(R.id.uid_tip);
        mUidView = (TextView) view.findViewById(R.id.uid);
        mUidView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mUidTip.setVisibility(View.VISIBLE);
                } else {
                    mUidTip.setVisibility(View.GONE);
                }
            }
        });

        // Salt
        final TextView saltTip = (TextView) view.findViewById(R.id.salt_tip);
        mSaltView = (EditText) view.findViewById(R.id.salt);
        mSaltView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                saltTip.setVisibility(View.VISIBLE);
            } else {
                saltTip.setVisibility(View.GONE);
            }
            }
        });

        // Password
        final TextView passwordTip = (TextView) view.findViewById(R.id.password_tip);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                passwordTip.setVisibility(View.VISIBLE);
            } else {
                passwordTip.setVisibility(View.GONE);
            }
            }
        });

        // Confirm password
        mConfirmPasswordView = (EditText) view.findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnFocusChangeListener(mPasswordView.getOnFocusChangeListener());
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


        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                attemptAddWallet();
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dismiss();
            }
        });

        return builder.create();
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

        // Read field values
        Currency currency = (Currency)getArguments().getSerializable(Currency.class.getSimpleName());
        if (currency == null) {
            currency = (Currency) mCurrencySpinner.getSelectedItem();
        }
        String name = mAliasView.getText().toString();
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
            mAliasView.setError(getString(R.string.name_or_uid_required));
            if (focusView == null) focusView = mAliasView;
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

        public AddWalletTask() {

            super(getActivity(), true/*use progress dialog*/);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtils.hideKeyboard(getActivity());
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

            long accountId = ((io.ucoin.app.Application) getActivity().getApplication()).getAccountId();

            // Create a seed from salt and password
            KeyPair keyPair = ServiceLocator.instance().getCryptoService().getKeyPair(salt, password);

            // Create a new wallet
            Wallet wallet = new Wallet(currency.getCurrencyName(), uid, keyPair.publicKey, keyPair.secretKey);
            wallet.setCurrencyId(currency.getId());
            wallet.setSalt(salt);
            wallet.setAccountId(accountId);
            wallet.setName(name);

            // Load membership
            ServiceLocator.instance().getBlockchainRemoteService().loadMembership(currency.getId(), wallet.getIdentity(), true);

            // Get credit
            TransactionRemoteService txService = ServiceLocator.instance().getTransactionRemoteService();
            Long credit = txService.getCredit(currency.getId(), wallet.getPubKeyHash());
            wallet.setCredit(credit == null ? 0 : credit);

            // Save the wallet in DB
            // (reset private key first)
            wallet.setSecKey(null);
            ServiceLocator.instance().getWalletService().save(getActivity(), wallet);

            return wallet;
        }

        @Override
        protected void onSuccess(Wallet wallet) {
            dismiss();
        }

        @Override
        protected void onFailed(Throwable t) {

            if (t instanceof DuplicatePubkeyException) {
                mUidView.setError(getString(R.string.duplicate_wallet_pubkey));
                mUidView.requestFocus();
            }
            else {
                Log.d(TAG, "Error in AddWalletTask", t);
                Toast.makeText(getActivity(),
                        ExceptionUtils.getMessage(t),
                        Toast.LENGTH_LONG).show();
            }

        }

        @Override
        protected void onCancelled() {
            dismiss();
        }
    }
}



