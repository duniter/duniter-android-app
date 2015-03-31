package io.ucoin.app.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.fragment.AddAccountFragment;
import io.ucoin.app.fragment.AddCurrencyFragment;
import io.ucoin.app.model.Account;
import io.ucoin.app.model.Peer;
import io.ucoin.app.service.AccountService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

public class AddAccountActivity extends ActionBarActivity  {

    private final String TAG = "AddAccountActivity";

    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private ProgressViewAdapter mProgressViewAdapter;
    private Bundle mResultBundle;
    private Peer mPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        mResultBundle = new Bundle();

        mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
        mProgressText = (TextView)findViewById(R.id.progress_text);
        mProgressViewAdapter = new ProgressViewAdapter(
                this,
                R.id.layout_progress,
                R.id.frame_content
        );

        // Progression welcome message (convert to HTML)
        TextView progressionTitle = (TextView)findViewById(R.id.progress_welcome);
        progressionTitle.setText(Html.fromHtml(getString(R.string.creating_account_welcome)));

        // First step : add account fragment
        Fragment fragment = AddAccountFragment.newInstance(new AddAccountFragment.OnClickListener() {
            @Override
            public void onPositiveClick(Bundle accountBundle) {
                mResultBundle.putAll(accountBundle);
                // Run second step
                showStep2();
            }
        });
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.fade_in,
                        R.animator.fade_out)
                .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    private void showStep2() {
        // Second step: add currency
        AddCurrencyFragment fragment = AddCurrencyFragment.newInstance(new AddCurrencyFragment.OnClickListener() {
            public void onPositiveClick(Bundle args) {
                mResultBundle.putAll(args);
                onFinishSteps();
            }
        });
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.slide_in_left,
                        R.animator.fade_out,
                        R.animator.slide_in_right,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * Call after step 2
     */
    private void onFinishSteps(){
        AddAccountTask task = new AddAccountTask(mProgressBar, mProgressText);
        task.execute(mResultBundle);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddAccountTask extends AsyncTaskHandleException<Bundle, Integer, io.ucoin.app.model.Account> {

        public AddAccountTask(ProgressBar progressBar, TextView progressText) {
            super(progressBar, progressText);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Hide the keyboard, in case we come from imeDone)
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == getCurrentFocus())
                            ? null
                            : getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            // Switch to the progress bar
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected io.ucoin.app.model.Account doInBackgroundHandleException(Bundle... bundles) throws Exception {
            Bundle bundle = bundles[0];
            // Read the result bundle
            String uid = bundle.getString("uid");
            String salt = bundle.getString("salt");
            String password = bundle.getString("password");
            Peer peer = (Peer) bundle.getSerializable(Peer.class.getSimpleName());

            // Creating account
            AccountService accountService = ServiceLocator.instance().getAccountService();
            Account account = accountService.create(
                    AddAccountActivity.this,
                    uid, salt, password, peer,
                    this);
            return account;
        }

        @Override
        protected void onSuccess(io.ucoin.app.model.Account account) {
            //restart MainActivity
            Intent intent = new Intent(AddAccountActivity.this, MainActivity.class);
            // TODO : give the wallet to main activity ?
            startActivity(intent);
            finish();
        }

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);
            Log.e(TAG, Log.getStackTraceString(t));
            Toast.makeText(AddAccountActivity.this,
                    ExceptionUtils.getMessage(t),
                    Toast.LENGTH_SHORT).show();

            // TODO : manage exception type, between :
            // DuplicatePubkeyException, UidMatchAnotherPubkeyException, PeerConnectionException
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

}