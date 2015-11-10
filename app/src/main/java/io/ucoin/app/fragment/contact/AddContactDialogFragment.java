package io.ucoin.app.fragment.contact;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.activity.SettingsActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.ContactUtils;
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

    private String mSaveContactsPref;

    public static AddContactDialogFragment newInstance(Identity identity) {
        AddContactDialogFragment fragment = new AddContactDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(Identity.class.getSimpleName(), identity);
        fragment.setArguments(args);

        return fragment;
    }

    // UI references.
    private TextView mAliasView;
    private ProgressViewAdapter mProgressViewAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSaveContactsPref = sharedPref.getString(SettingsActivity.PREF_CONTACT_SAVE_KEY, String.valueOf(SettingsActivity.PREF_CONTACT_SAVE_IN_APP));

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_add_new_contact, null);
        builder.setView(view);
        builder.setTitle(R.string.add_as_new_contact_title);

        // Getting a given identity
        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        // Confirmation message
        TextView confirmationMessageView = (TextView) view.findViewById(R.id.confirm_message);
        if (StringUtils.isNotBlank(identity.getUid())) {
            confirmationMessageView.setText(Html.fromHtml(
                    getString(R.string.add_contact_confirm, identity.getUid())));

        }
        else if (StringUtils.isNotBlank(identity.getPubkey())) {
            confirmationMessageView.setText(Html.fromHtml(
                    getString(R.string.add_contact_confirm, StringUtils.truncate(identity.getPubkey(), 6))));
        }
        else {
            confirmationMessageView.setVisibility(View.GONE);
        }

        // Alias
        mAliasView = (TextView) view.findViewById(R.id.alias);
        mAliasView.requestFocus();
        mAliasView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        mAliasView.setError(null);

        // Store values at the time of the login attempt.
        String alias = mAliasView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid alias (mandatory if uid is not set)
        if (TextUtils.isEmpty(alias)) {
            // Use UID as default alias
            if (!TextUtils.isEmpty(identity.getUid())) {
                alias = identity.getUid();
            }
            else {
                mAliasView.setError(getString(R.string.field_required));
                if (focusView == null) focusView = mAliasView;
                cancel = true;
            }
        }
        else if (!isNameValid(alias)) {
            mAliasView.setError(getString(R.string.name_too_short));
            if (focusView == null) focusView = mAliasView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Will show the progress bar, and create the wallet
            AddContactTask task = new AddContactTask();
            task.execute(identity, alias);
        }
    }

    private boolean isNameValid(String name) {
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
            contact.setPhoneContactId((long) 0);

            // Save the contact in DB
            ServiceLocator.instance().getContactService().save(getContext(), contact,true);

            if(mSaveContactsPref.equals(String.valueOf(SettingsActivity.PREF_CONTACT_SAVE_IN_PHONE))) {
                addNewContactInPhone(name, ContactUtils.createUri(identity));
            }

            return contact;
        }

        public void addNewContactInPhone(String name, String url){

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

            intent.putExtra(ContactsContract.Intents.Insert.NAME, name);

            ArrayList<ContentValues> data = new ArrayList<ContentValues>();
            ContentValues row1 = new ContentValues();

            row1.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            row1.put(ContactsContract.CommonDataKinds.Website.URL, url);
            //row1.put(ContactsContract.CommonDataKinds.Website.LABEL, "abc");
            row1.put(ContactsContract.CommonDataKinds.Website.TYPE, ContactsContract.CommonDataKinds.Website.TYPE_HOME);
            data.add(row1);
            intent.putExtra(ContactsContract.Intents.Insert.DATA, data);
            intent.putExtra ("finishActivityOnSaveCompleted", true);
//              Uri dataUri = getActivity().getContentResolver().insert(ContactsContract.Data.CONTENT_URI, row1);
            getContext().startActivity(intent);
            //------------------------------- end of inserting contact in the phone
        }



        @Override
        protected void onSuccess(Contact contact) {
            dismiss();
            Toast.makeText(getContext(),
                    getString(R.string.contact_added),
                    Toast.LENGTH_SHORT).show();
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



