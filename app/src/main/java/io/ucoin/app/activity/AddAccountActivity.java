package io.ucoin.app.activity;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.fragment.AddAccountFragment;
import io.ucoin.app.fragment.AddCurrencyFragment;
import io.ucoin.app.model.Account;
import io.ucoin.app.model.Peer;
import io.ucoin.app.service.AccountService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;

public class AddAccountActivity extends ActionBarActivity  {

    private final String TAG = "AddAccountActivity";

    private ProgressViewAdapter mProgressViewAdapter;
    private Bundle mResultBundle;
    private Peer mPeer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        mResultBundle = new Bundle();

        mProgressViewAdapter = new ProgressViewAdapter(
                this,
                R.id.load_progress,
                R.id.frame_content
        );

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
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * Call when account created (after step 2)
     */
    private void onFinishSteps(){
        AddAccountTask task = new AddAccountTask();
        task.execute(mResultBundle);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddAccountTask extends AsyncTaskHandleException<Bundle, Void, io.ucoin.app.model.Account> {

        @Override
        protected void onPreExecute() {
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

            // Create account in DB
            AccountService accountService = ServiceLocator.instance().getAccountService();
            Account account = accountService.create(AddAccountActivity.this,
                    uid, salt, password, peer);
            return account;
        }

        @Override
        protected void onSuccess(io.ucoin.app.model.Account account) {
            //restart MainActivity
            Intent intent = new Intent(AddAccountActivity.this, MainActivity.class);
            // TODO : give the wallet ?
            startActivity(intent);
            finish();
        }

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);
            Log.e(TAG, Log.getStackTraceString(t));
            Toast.makeText(AddAccountActivity.this,
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

}