package io.ucoin.app.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;

public class AddAccountFragment extends Fragment {

    private TextView mUidHint;
    private TextView mSaltHint;
    private TextView mPasswordHint;
    private EditText mUidView;
    private EditText mSaltView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private OnClickListener mListener;

    public static AddAccountFragment newInstance(OnClickListener listener) {
        AddAccountFragment fragment = new AddAccountFragment();
        fragment.setOnClickListener(listener);
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


        mUidHint = (TextView) view.findViewById(R.id.uid_tip);
        mSaltHint = (TextView) view.findViewById(R.id.salt_tip);
        mPasswordHint = (TextView) view.findViewById(R.id.password_tip);

        mUidView = (EditText) view.findViewById(R.id.uid);
        mSaltView = (EditText) view.findViewById(R.id.salt);
        mPasswordView = (EditText) view.findViewById(R.id.password);

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

        mConfirmPasswordView = (EditText) view.findViewById(R.id.confirm_password);
        mConfirmPasswordView.setOnFocusChangeListener(mPasswordView.getOnFocusChangeListener());

        mConfirmPasswordView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return attemptAddAccount();
                }
                return false;
            }
        });

        // TODO FOR DEV ONLY (to remove later)
        {

        }
    }

    public boolean attemptAddAccount() {

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

        String confirmPassword = mConfirmPasswordView.getText().toString();
        if (confirmPassword.isEmpty()) {
            mConfirmPasswordView.setError(getString(R.string.confirm_password_cannot_be_empty));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if(!password.equals(confirmPassword)) {
            mPasswordView.setError(getString(R.string.passwords_dont_match));
            mConfirmPasswordView.setError(getString(R.string.passwords_dont_match));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
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



