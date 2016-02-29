package io.ucoin.app.fragment.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.task.GenerateKeysTask;
import io.ucoin.app.technical.crypto.KeyPair;

public class AddIdentityDialogFragment extends DialogFragment {

    private CurrencyActivity mActivity;
    private LinearLayout mFormLayout;
    private LinearLayout mProgressLayout;

    public static AddIdentityDialogFragment newInstance(Long currencyId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, currencyId);
        AddIdentityDialogFragment fragment = new AddIdentityDialogFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.dialog_fragment_add_identity, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mFormLayout = (LinearLayout) view.findViewById(R.id.form_layout);
        mProgressLayout = (LinearLayout) view.findViewById(R.id.progress_layout);



        mActivity = (CurrencyActivity) getActivity();


        final TextView instructions = (TextView) view.findViewById(R.id.instructions);
        final TextView uid = (EditText) view.findViewById(R.id.uid);
        final TextView salt = (EditText) view.findViewById(R.id.salt);
        final EditText password = (EditText) view.findViewById(R.id.password);
        final EditText confirmPassword = (EditText) view.findViewById(R.id.confirm_password);
        final Button posButton = (Button) view.findViewById(R.id.positive_button);
        final Button cancelButton = (Button) view.findViewById(R.id.cancel_button);


        uid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    instructions.setText(getActivity().getString(R.string.uid_instructions));
                    instructions.setVisibility(View.VISIBLE);
                } else {
                    instructions.setText("");
                    instructions.setVisibility(View.GONE);
                }
            }
        });
        salt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    instructions.setText(getActivity().getString(R.string.salt_instructions));
                    instructions.setVisibility(View.VISIBLE);
                } else {
                    instructions.setText("");
                    instructions.setVisibility(View.GONE);
                }
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    instructions.setText(getActivity().getString(R.string.password_instructions));
                    instructions.setVisibility(View.VISIBLE);
                } else {
                    instructions.setText("");
                    instructions.setVisibility(View.GONE);
                }
            }
        });

        confirmPassword.setOnFocusChangeListener(password.getOnFocusChangeListener());
        confirmPassword.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    posButton.performClick();
                    return true;
                }
                return false;
            }
        });


        posButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate UIDt
                if (uid.getText().toString().isEmpty()) {
                    uid.setError(getString(R.string.uid_cannot_be_empty));
                    return;
                }

                //validate salt
                if (salt.getText().toString().isEmpty()) {
                    salt.setError(getString(R.string.salt_cannot_be_empty));
                    return;
                }

                //validate password
                if (password.getText().toString().isEmpty()) {
                    password.setError(getString(R.string.password_cannot_be_empty));
                    return;
                }

                if (confirmPassword.getText().toString().isEmpty()) {
                    confirmPassword.setError(getString(R.string.confirm_password_cannot_be_empty));
                    return;
                }

                if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                    password.setError(getString(R.string.passwords_dont_match));
                    confirmPassword.setError(getString(R.string.passwords_dont_match));
                    return;
                }

                GenerateKeysTask task = new GenerateKeysTask(new GenerateKeysTask.OnTaskFinishedListener() {
                    @Override
                    public void onTaskFinished(KeyPair keyPair) {
                        UcoinCurrency currency = new Currency(getActivity(), getArguments().getLong(BaseColumns._ID));

                        try {
                            //UcoinIdentity identity = currency.addIdentity(uid.getText().toString(), keyPair.getPubKey().toString());
                            dismiss();
                        } catch (Exception e) {
                            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        Application.requestSync();
                    }
                });

                Bundle args = new Bundle();
                args.putString("salt", salt.getText().toString());
                args.putString("password", password.getText().toString());
                mFormLayout.setVisibility(View.GONE);
                mProgressLayout.setVisibility(View.VISIBLE);
                task.execute(args);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}



