package io.ucoin.app.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;
import android.widget.EditText;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.IdentityListAdapter;
import io.ucoin.app.exception.UncaughtExceptionHandler;
import io.ucoin.app.model.BasicIdentity;
import io.ucoin.app.model.WotLookupResults;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.WotService;
import io.ucoin.app.technical.AsyncTaskHandleException;

public class WotSearchActivity extends ListActivity {

    private static final String TAG = "WotSearchActivity";
    private static final List<BasicIdentity> EMPTY_LIST = new ArrayList<BasicIdentity>(0);
    private static final int MIN_SEARCH_CHARACTERS = 3;

    private EditText mSearchView;
    private View mProgressView;
    private ListView mList;
    private IdentityListAdapter mIdentityListAdapter;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SearchTask mSearchTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));

        setContentView(R.layout.activity_search);

        mSearchView = (EditText) findViewById(R.id.search);

        mProgressView= (View) findViewById(R.id.search_progress);

        mList = (ListView)getListView();

        mIdentityListAdapter = new IdentityListAdapter(EMPTY_LIST, false) {
            @Override
            protected LayoutInflater getLayoutInflater() {
                return WotSearchActivity.this.getLayoutInflater();
            }
        };
        setListAdapter(mIdentityListAdapter);

        mSearchView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

                // When user changed the Text: run search
                if (cs.length() >= MIN_SEARCH_CHARACTERS) {
                    doSearch();
                }
                else {
                    mIdentityListAdapter.setIdentities(EMPTY_LIST);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        BasicIdentity identity = (BasicIdentity)l.getAdapter().getItem(position);
        Log.d(TAG, "click on " + identity.getUid() + "/" + identity.getPubkey());

    }

    protected void doSearch() {

        if (mSearchTask != null) {
            return;
        }

        // Reset errors.
        mSearchView.setError(null);

        // Store values at the time of the login attempt.
        String searchQuery = mSearchView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid uid
        if (TextUtils.isEmpty(searchQuery)) {
            mIdentityListAdapter.setIdentities(EMPTY_LIST);
            focusView = mSearchView;
            cancel = true;
        } else if (searchQuery.length() <= MIN_SEARCH_CHARACTERS) {
            focusView = mSearchView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mSearchTask = new SearchTask(searchQuery);
            mSearchTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SearchTask extends AsyncTaskHandleException<Void, Void, List<BasicIdentity>> {

        private final String mSearchQuery;

        SearchTask(String mSearchQuery) {
            this.mSearchQuery = mSearchQuery;
        }

        @Override
        protected List<BasicIdentity> doInBackgroundHandleException(Void... params) {

            WotService service = ServiceLocator.instance().getWotService();
            WotLookupResults results = service.find(mSearchQuery);

            if (results == null) {
                return null;
            }

            return service.toIdentities(results);
        }

        @Override
        protected void onSuccess(List<BasicIdentity> identities) {
            mSearchTask = null;
            showProgress(false);

            if (identities == null || identities.size() == 0) {
                // TODO translate
                mSearchView.setError("No user found");
                mSearchView.requestFocus();
                mIdentityListAdapter.setIdentities(EMPTY_LIST);
                return;
            }

            mIdentityListAdapter.setIdentities(identities);
        }

        @Override
        protected void onFailed(Throwable t) {
            mSearchView.setError(t.getMessage());
            mSearchView.requestFocus();
            mIdentityListAdapter.setIdentities(EMPTY_LIST);
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mSearchTask = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mList.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mList.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mList.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
