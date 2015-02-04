package io.ucoin.app.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;


//todo validate inputs
public class CreateAccountFragment extends Fragment implements TextView.OnEditorActionListener {

    private EditText mSalt;
    private EditText mPassword;
    private EditText mConfirmPassword;

    public static CreateAccountFragment newInstance() {
        return new CreateAccountFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_create_account,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSalt = (EditText) view.findViewById(R.id.salt);
        mPassword = (EditText) view.findViewById(R.id.password);
        mConfirmPassword = (EditText) view.findViewById(R.id.confirm_password);

        mConfirmPassword.setOnEditorActionListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle("");
        ((MainActivity) getActivity()).setBackButtonEnabled(true);
    }

    //Return false to allow normal menu processing to proceed, true to consume it here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != EditorInfo.IME_ACTION_DONE) {
            return false;
        }

        //todo generate public key and signature and create account or import
        //and make verifications etc...

        //Create account in database
        ContentValues values = new ContentValues();
        values.put(Contract.Account.UID, "dummyaccount");
        values.put(Contract.Account.PUBLIC_KEY, "4YK1VbKzm1CDFBrks7YAU2rcuW1guFvQnGLpxTL31CTd");
        //the private key is 5Xn7fELqSzFSsKnoHTHJ6ZV4YXXUqHgyxufNAiCuXp9v4wmSB2j8rmy5sNekDa5qBcR6y7A84Csc9Qze9GmmceJF

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/");
        uri = getActivity().getContentResolver().insert(uri, values);

        //create account in adnroid framework
        Bundle data = new Bundle();
        data.putString("_id", Long.toString(ContentUris.parseId(uri)));
        Account account = new Account("dummyaccount", getString(R.string.ACCOUNT_TYPE));
        AccountManager.get(getActivity()).addAccountExplicitly(account, null, data);

        //create last account in shared preferences
        //so we can keep the last account used in memory
        //on the next activity bootup
        SharedPreferences.Editor editor = getActivity()
                .getSharedPreferences("account", Context.MODE_PRIVATE).edit();
        editor.putString("_id", Long.toString(ContentUris.parseId(uri)));
        editor.commit();

        account = ((MainActivity) getActivity()).loadAccount();
        if (account == null)
        {
            Log.e("CREATEACCOUNTFRAGMENT", "Could not load account");
            getActivity().finish();
        }

        Fragment fragment = HomeFragment.newInstance();
        getFragmentManager().popBackStack();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.fade_in,
                        R.animator.fade_out)
                .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
        return true;
    }
}
