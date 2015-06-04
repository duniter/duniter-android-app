package io.ucoin.app.activity;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.fragment.account.AddAccountFragment;
import io.ucoin.app.fragment.account.AddCurrencyFragment;
import io.ucoin.app.model.local.Account;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.service.exception.PubkeyAlreadyUsedException;
import io.ucoin.app.service.exception.UidAlreadyUsedException;
import io.ucoin.app.service.exception.UidAndPubkeyNotFoundException;
import io.ucoin.app.service.exception.UidMatchAnotherPubkeyException;
import io.ucoin.app.service.local.AccountService;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;

public class AddAccountActivity extends ActionBarActivity  {

    private final static String BUNDLE_REGISTER_NEW = "REGISTER_NEW";
    private final static String BUNDLE_ERROR = "ERROR";
    private final String TAG = "AddAccountActivity";

    private TextView mTitleText;
    private TextView mWelcomeText;
    private View mProgressLayout;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private View mToolbarLayout;
    private Bundle mResultBundle;
    private int mAnimTime;
    private AddAccountTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        mAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mResultBundle = new Bundle();

        mProgressBar = (ProgressBar)findViewById(R.id.progressbar);
        mProgressText = (TextView)findViewById(R.id.progress_text);
        mToolbarLayout = findViewById(R.id.layout_toolbar);
        mProgressLayout = findViewById(R.id.layout_progress);

        // Title
        mTitleText = (TextView)findViewById(R.id.title);

        // Welcome message (convert to HTML)
        mWelcomeText = (TextView)findViewById(R.id.welcome_message);
        mWelcomeText.setText(Html.fromHtml(getString(R.string.creating_account_welcome)));

        // Login button
        Button loginButton = (Button)findViewById(R.id.button_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultBundle.putBoolean(BUNDLE_REGISTER_NEW, false);
                showStep1();
            }
        });

        // Register button
        Button registerButton = (Button)findViewById(R.id.button_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultBundle.putBoolean(BUNDLE_REGISTER_NEW, true);
                showStep1();
            }
        });

    }


    @Override
    public void onBackPressed() {

        if (mTask != null) {
            mTask.cancel();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        int bsEntryCount = fragmentManager.getBackStackEntryCount();
        if (bsEntryCount <= 1) {

            if (mToolbarLayout.getVisibility() == View.VISIBLE) {
                finish();
                return;
            }

            fragmentManager.popBackStack();
            showWelcome();
        }
        else {
            fragmentManager.popBackStack();
        }
    }

    private void showWelcome() {
        mResultBundle.clear();

        // Show
        ViewUtils.setVisibleWithAnimation(mTitleText, true);
        ViewUtils.setVisibleWithAnimation(mWelcomeText, true);
        ViewUtils.setVisibleWithAnimation(mToolbarLayout, true);

        // Hide
        ViewUtils.setVisibleWithAnimation(mProgressLayout, false);
        ViewUtils.setVisibleWithAnimation(findViewById(R.id.layout_fragment), false);
    }

    private void showProgressBar() {
        // Show
        ViewUtils.setVisibleWithAnimation(mTitleText, true);
        ViewUtils.setVisibleWithAnimation(mWelcomeText, true);
        ViewUtils.setVisibleWithAnimation(mProgressLayout, true);

        // Hide
        ViewUtils.setVisibleWithAnimation(mToolbarLayout, false);
        ViewUtils.setVisibleWithAnimation(findViewById(R.id.layout_fragment), false);
    }

    private void showFragment() {
        // Show
        ViewUtils.setVisibleWithAnimation(findViewById(R.id.layout_fragment), true);

        // Hide
        ViewUtils.setVisibleWithAnimation(mTitleText, false);
        ViewUtils.setVisibleWithAnimation(mWelcomeText, false);
        ViewUtils.setVisibleWithAnimation(mProgressLayout, false);
        ViewUtils.setVisibleWithAnimation(mToolbarLayout, false);
    }

    private void showStep1() {

        FragmentManager fragmentManager = getSupportFragmentManager();

        // Second step: add currency
        AddCurrencyFragment fragment = AddCurrencyFragment.newInstance(new AddCurrencyFragment.OnClickListener() {
            public void onPositiveClick(Bundle args) {
                mResultBundle.putAll(args);

                // Run second step
                showStep2();
            }
        }, mResultBundle);
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(fragment.getClass().getSimpleName(), 0);
        }
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.fade_in,
                        R.animator.fade_out)
                .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        // Show the fragment
        showFragment();
    }

    private void showStep2() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // First step : add account fragment
        Fragment fragment = AddAccountFragment.newInstance(new AddAccountFragment.OnClickListener() {
            @Override
            public void onPositiveClick(Bundle args) {
                mResultBundle.putAll(args);

                onFinishSteps();
            }
        }, mResultBundle);
        fragmentManager.popBackStack(fragment.getClass().getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.slide_in_right,
                        R.animator.fade_out,
                        R.animator.slide_in_left,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * Call after step 2
     */
    private void onFinishSteps(){
        if (mTask == null) {
            mTask = new AddAccountTask(mProgressBar, mProgressText);
            mTask.execute(mResultBundle);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class AddAccountTask extends AsyncTaskHandleException<Bundle, Integer, Account> {

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

            // Switch to progress bar (mask the toolbar)
            showProgressBar();
        }

        @Override
        protected Account doInBackgroundHandleException(Bundle... bundles) throws Exception {
            Bundle bundle = bundles[0];

            // Read the result bundle
            boolean isNewRegistration = bundle.getBoolean(BUNDLE_REGISTER_NEW);
            String uid = bundle.getString("uid");
            String salt = bundle.getString("salt");
            String password = bundle.getString("password");
            Peer peer = (Peer) bundle.getSerializable(Peer.class.getSimpleName());

            // Creating account
            AccountService accountService = ServiceLocator.instance().getAccountService();
            Account account = accountService.create(
                    getContext(),
                    uid, salt, password, isNewRegistration, peer,
                    this);

            // Loading caches
            increment(getContext().getString(R.string.starting_home));
            ServiceLocator.instance().loadCaches(getContext(), account.getId());

            return account;
        }

        @Override
        protected void onSuccess(Account account) {
            mTask = null;
            //restart MainActivity
            Intent intent = new Intent(AddAccountActivity.this, MainActivity.class);
            // TODO : give the wallet to main activity ?
            startActivity(intent);
            finish();
        }

        @Override
        protected void onFailed(Throwable t) {
            mTask = null;

            FragmentManager fragmentManager = getSupportFragmentManager();

            if (t instanceof UidMatchAnotherPubkeyException
              || t instanceof UidAlreadyUsedException
              || t instanceof PubkeyAlreadyUsedException
              || t instanceof UidAndPubkeyNotFoundException
              || t instanceof PeerConnectionException){

                if (t instanceof PeerConnectionException) {
                    fragmentManager.popBackStack(0, 0);
                }
                else {
                    fragmentManager.popBackStack(1, 0);
                }

                // Give the args to fragment
                mResultBundle.putSerializable(BUNDLE_ERROR, t);
                Fragment fragment = fragmentManager.findFragmentById(R.id.frame_content);
                if (fragment != null) {
                    fragmentManager.popBackStack();
                    if (t instanceof PeerConnectionException) {
                       showStep1();
                    }
                    else {
                        showStep2();
                        showFragment();
                    }
                }
            }
            else {
                fragmentManager.popBackStack();

                Log.e(TAG, Log.getStackTraceString(t));
                Toast.makeText(AddAccountActivity.this,
                        ExceptionUtils.getMessage(t),
                        Toast.LENGTH_SHORT).show();

                showFragment();
            }
        }

        @Override
        protected void onCancelled() {
            mTask = null;
        }
    }

}