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
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.Views;
import io.ucoin.app.model.Contact;
import io.ucoin.app.model.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;

/**
 * A screen used to add a wallet via currency, uid, salt and password.
 */
public class AddContactFragment extends Fragment {

    public static final String TAG = "AddContactFragment";

    public static AddContactFragment newInstance(Identity identity) {
        AddContactFragment fragment = new AddContactFragment();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_add_new_contact,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Getting a given identity
        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        // Uid
        mUidView = (TextView) view.findViewById(R.id.uid);

        // Name
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

        Button mAddButton = (Button) view.findViewById(R.id.add_button);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddContact(identity);
            }
        });

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                mAddButton);

        // fill the UI with the given identity
        updateView(identity);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.add_as_new_contact);
        ((MainActivity) getActivity()).setBackButtonEnabled(true);
    }

    private void updateView(Identity identity) {
        if (identity != null) {
            mUidView.setText(identity.getUid());
        }
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
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.field_required));
            if (focusView == null) focusView = mNameView;
            cancel = true;
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

        @Override
        protected void onPreExecute() {
            Views.hideKeyboard(getActivity());

            // Show the progress bar
            mProgressViewAdapter.showProgress(true);
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

            // Retrieve account id
            String accountId = ((io.ucoin.app.Application) getActivity().getApplication()).getAccountId();

            Contact contact = new Contact();
            contact.setName(name);
            contact.setAccountId(Long.valueOf(accountId));
            contact.addIdentity(identity);

            // Save the contact in DB
            ServiceLocator.instance().getContactService().save(getActivity(), contact);

            return contact;
        }

        @Override
        protected void onSuccess(Contact contact) {
            // Go back
            getFragmentManager().popBackStack();
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);

            Log.d(TAG, "Error in AddContactTask", t);
            Toast.makeText(getActivity(),
                    ExceptionUtils.getMessage(t),
                    Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }
}



