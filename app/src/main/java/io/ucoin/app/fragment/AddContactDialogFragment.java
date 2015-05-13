package io.ucoin.app.fragment;

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
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Contact;
import io.ucoin.app.model.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

/**
 * A screen used to add a wallet via currency, uid, salt and password.
 */
public class AddContactDialogFragment extends DialogFragment {

    public static final String TAG = "AddContactFragment";

    public static AddContactDialogFragment newInstance(Identity identity) {
        AddContactDialogFragment fragment = new AddContactDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(Identity.class.getSimpleName(), identity);
        fragment.setArguments(args);

        return fragment;
    }

    // UI references.
    private TextView mNameView;
    private TextView mUidView;
    private ProgressViewAdapter mProgressViewAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_new_contact, null);
        builder.setView(view);
        builder.setTitle(R.string.add_as_new_contact_title);

        // Getting a given identity
        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        // Uid
        mUidView = (TextView) view.findViewById(R.id.uid);
        if (identity != null) {
            mUidView.setText(identity.getUid());
        }

        // Alias
        mNameView = (TextView) view.findViewById(R.id.name);
        mNameView.requestFocus();
        mNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptAddContact(identity);
                    return true;
                }
                return false;
            }
        });


        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                attemptAddContact(identity);
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
    public void attemptAddContact(final Identity identity) {

        // Reset errors.
        mUidView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name (mandatory if uid is not set)
        if (!TextUtils.isEmpty(name)) {
            // Use UID as default name
            if (!TextUtils.isEmpty(identity.getUid())) {
                name = identity.getUid();
            }
            else {
                mNameView.setError(getString(R.string.field_required));
                if (focusView == null) focusView = mNameView;
                cancel = true;
            }
        }
        else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.name_too_short));
            if (focusView == null) focusView = mNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Will show the progress bar, and create the wallet
            AddContactTask task = new AddContactTask();
            task.execute(identity, name);
        }
    }

    private boolean isNameValid(String name) {
        // TODO : voir s'il y a une taille mininum
        return name.length() >= 2;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddContactTask extends AsyncTaskHandleException<Object, Void, Contact> {

        private Long mAccountId;

        public AddContactTask() {
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtils.hideKeyboard(getActivity());

            // Retrieve account id
            mAccountId = ((io.ucoin.app.Application) getActivity().getApplication()).getAccountId();
        }

        @Override
        protected Contact doInBackgroundHandleException(Object... args) throws Exception {
            ObjectUtils.checkNotNull(args);
            ObjectUtils.checkArgument(args.length == 2);

            Identity identity = (Identity)args[0];
            String name = (String)args[1];

            // Compute a name is not set
            if (StringUtils.isBlank(name)) {
                name = identity.getUid();
            }

            Contact contact = new Contact();
            contact.setName(name);
            contact.setAccountId(mAccountId);
            contact.addIdentity(identity);

            // Save the contact in DB
            ServiceLocator.instance().getContactService().save(getContext(), contact);

            return contact;
        }

        @Override
        protected void onSuccess(Contact contact) {
            dismiss();
        }

        @Override
        protected void onFailed(Throwable t) {
            Log.d(TAG, "Error in AddContactTask", t);
            Toast.makeText(getContext(),
                    ExceptionUtils.getMessage(t),
                    Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onCancelled() {
            dismiss();
        }
    }
}



