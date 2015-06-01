package io.ucoin.app.fragment.wallet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.CurrencyArrayAdapter;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.local.CurrencyService;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.ViewUtils;

/**
 * A screen used to add a wallet via currency, uid, salt and password.
 */
public class AddWalletDialogFragment extends DialogFragment {

    private static final String TAG = "AddWalletDialog";

    public static final String BUNDLE_ALIAS = "alias";
    public static final String BUNDLE_UID = "uid";
    public static final String BUNDLE_SALT = "salt";
    public static final String BUNDLE_PASSWORD = "password";
    public static final String BUNDLE_CURRENCY = Currency.class.getSimpleName();

    private OnClickListener mListener;

    public static AddWalletDialogFragment newInstance(Activity activity, OnClickListener listener) {
        ObjectUtils.checkNotNull(activity);
        ObjectUtils.checkNotNull(listener);

        // If only ONE currency in database, force to select this one
        CurrencyService currencyService = ServiceLocator.instance().getCurrencyService();
        int currencyCount = currencyService.getCurrencyCount();
        if (currencyCount == 1) {
            return newInstance(currencyService.getCurrencies(activity).iterator().next(), listener);
        }

        AddWalletDialogFragment fragment = new AddWalletDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        fragment.setOnClickListener(listener);

        return fragment;
    }

    public static AddWalletDialogFragment newInstance(Currency currency, OnClickListener listener) {
        ObjectUtils.checkNotNull(currency);
        ObjectUtils.checkNotNull(listener);

        AddWalletDialogFragment fragment = new AddWalletDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(Currency.class.getSimpleName(), currency);
        fragment.setArguments(args);

        fragment.setOnClickListener(listener);

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
        /*mUidView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    mUidTip.setVisibility(View.VISIBLE);
                } else {
                    mUidTip.setVisibility(View.GONE);
                }
            }
        });*/

        // Salt
        final TextView saltTip = (TextView) view.findViewById(R.id.salt_tip);
        mSaltView = (EditText) view.findViewById(R.id.salt);
        /*mSaltView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                saltTip.setVisibility(View.VISIBLE);
            } else {
                saltTip.setVisibility(View.GONE);
            }
            }
        });*/

        // Password
        final TextView passwordTip = (TextView) view.findViewById(R.id.password_tip);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        /*mPasswordView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus) {
                passwordTip.setVisibility(View.VISIBLE);
            } else {
                passwordTip.setVisibility(View.GONE);
            }
            }
        });*/

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
        String alias = mAliasView.getText().toString();
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

        // Check for a valid alias (mandatory if uid is not set)
        if (TextUtils.isEmpty(alias) && TextUtils.isEmpty(uid)) {
            mAliasView.setError(getString(R.string.alias_or_uid_required));
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
            Bundle args = new Bundle();
            args.putSerializable(Currency.class.getSimpleName(), currency);
            args.putString(BUNDLE_ALIAS, alias);
            args.putString(BUNDLE_UID, uid);
            args.putString(BUNDLE_SALT, salt);
            args.putString(BUNDLE_PASSWORD, password);
            mListener.onPositiveClick(args);

            ViewUtils.hideKeyboard(getActivity());

            dismiss();
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

    private void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }
}



