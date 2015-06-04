package io.ucoin.app.fragment.account;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.service.exception.PubkeyAlreadyUsedException;
import io.ucoin.app.service.exception.UidAlreadyUsedException;
import io.ucoin.app.service.exception.UidAndPubkeyNotFoundException;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;

public class AddAccountFragment extends Fragment {

    private static final String BUNDLE_ERROR = "ERROR";
    private static final String BUNDLE_REGISTER_NEW = "REGISTER_NEW";

    private TextView mUidHint;
    private TextView mSaltHint;
    private TextView mPasswordHint;
    private EditText mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private OnClickListener mListener;

    public static AddAccountFragment newInstance(OnClickListener listener, Bundle args) {
        AddAccountFragment fragment = new AddAccountFragment();
        fragment.setOnClickListener(listener);
        Bundle inputArgs = new Bundle();
        inputArgs.putAll(args);
        fragment.setArguments(inputArgs);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_add_account,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        final boolean useConfirmPassword = args.getBoolean(BUNDLE_REGISTER_NEW);

        mUidHint = (TextView) view.findViewById(R.id.uid_tip);
        mSaltHint = (TextView) view.findViewById(R.id.salt_tip);
        mPasswordHint = (TextView) view.findViewById(R.id.password_tip);

        mUidView = (EditText) view.findViewById(R.id.uid);
        mUidView.requestFocus();

        mSaltView = (EditText) view.findViewById(R.id.salt);
        mPasswordView = (EditText) view.findViewById(R.id.password);
        mConfirmPasswordView = (EditText) view.findViewById(R.id.confirm_password);

        // Login button
        Button loginButton = (Button)view.findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAddAccount(useConfirmPassword);
            }
        });

        // If register as new user
        if (useConfirmPassword) {

            // Display help tooltips on focus
            /*mUidView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
            mConfirmPasswordView.setOnFocusChangeListener(mPasswordView.getOnFocusChangeListener());
            */

            // Set button text
            loginButton.setText(getString(R.string.register));

            TextView titleView = (TextView) view.findViewById(R.id.title);
            titleView.setText(getString(R.string.register));

            // Manage IME action of the confirm password view
            mConfirmPasswordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        return attemptAddAccount(true);
                    }
                    return false;
                }
            });
        }

        // If only a login screen
        else {
            // Mask the confirmation password view
            mConfirmPasswordView.setVisibility(View.GONE);

            // Set IME action of the password
            mPasswordView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            mPasswordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId,
                                              KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        return attemptAddAccount(false);
                    }
                    return false;
                }
            });
        }


        // Bind input args to view
        bindViews(args);
    }


    protected void bindViews(Bundle args) {
        String uid = (String)args.getSerializable("uid");
        String salt = (String)args.getSerializable("salt");

        mUidView.setText(uid);
        mSaltView.setText(salt);

        Throwable error = (Throwable)args.getSerializable(BUNDLE_ERROR);
        if (error != null) {
            View focusView = null;
            boolean keepPassword = true;

            if (error instanceof UidMatchAnotherPubkeyException) {
                mPasswordView.setError(getString(R.string.uid_match_another_pubkey));
                // clear password
                keepPassword = false;
                focusView = mPasswordView;
            } else if (error instanceof UidAlreadyUsedException) {
                mUidView.setError(getString(R.string.uid_already_used));
                focusView = mUidView;
            } else if (error instanceof PubkeyAlreadyUsedException) {
                mPasswordView.setError(getString(R.string.pubkey_already_used));
                focusView = mPasswordView;
                // clear password
                keepPassword = false;
            } else if (error instanceof UidAndPubkeyNotFoundException) {
                mUidView.setError(getString(R.string.uid_and_pubkey_not_registred));
                focusView = mUidView;
            } else {
                // TODO
            }

            if (keepPassword) {
                String password = (String) args.getSerializable("password");
                mPasswordView.setText(password);
                mConfirmPasswordView.setText(password);
            }
            else {
                args.remove("password");
                mPasswordView.setText("");
                mConfirmPasswordView.setText("");
            }

            if (focusView != null) {
                focusView.requestFocus();
            }
        }
    }

    protected boolean attemptAddAccount(boolean checkConfirmPassword) {

        boolean cancel = false;
        View focusView = null;

        //validate uid
        String uid = mUidView.getText().toString();
        if (uid.isEmpty()) {
            mUidView.setError(getString(R.string.uid_cannot_be_empty));
            focusView = mUidView;
            cancel = true;
        }
        //validate salt
        String salt = mSaltView.getText().toString();
        if (salt.isEmpty()) {
            mSaltView.setError(getString(R.string.salt_cannot_be_empty));
            focusView = mSaltView;
            cancel = true;
        }

        //validate password
        String password = mPasswordView.getText().toString();
        if (password.isEmpty()) {
            mPasswordView.setError(getString(R.string.password_cannot_be_empty));
            focusView = mPasswordView;
            cancel = true;
        }

        if (checkConfirmPassword) {
            String confirmPassword = mConfirmPasswordView.getText().toString();
            if (confirmPassword.isEmpty()) {
                mConfirmPasswordView.setError(getString(R.string.confirm_password_cannot_be_empty));
                focusView = mConfirmPasswordView;
                cancel = true;
            }

            if (!password.equals(confirmPassword)) {
                mPasswordView.setError(getString(R.string.passwords_dont_match));
                mConfirmPasswordView.setError(getString(R.string.passwords_dont_match));
                focusView = mConfirmPasswordView;
                cancel = true;
            }
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return false;
        }

        // Send result to listener
        if (mListener != null) {
            // Send the result to the listener
            Bundle args = new Bundle();
            args.putSerializable("uid", uid);
            args.putSerializable("salt", salt);
            args.putSerializable("password", password);
            mListener.onPositiveClick(args);
        }
        return true;
    }

    private void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        public void onPositiveClick(Bundle args);
    }
}



