package io.ucoin.app.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.IdentityListAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.exception.UncaughtExceptionHandler;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.WotLookupResults;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.WotService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.DateUtils;

public class WotSearchActivity extends ListActivity {

    private static final String PARAM_CHOICE_MODE = "choiceMode";

    private static final String TAG = "WotSearchActivity";
    private static final int MIN_SEARCH_CHARACTERS = 2;

    private SearchView mSearchView;
    private ListView mList;
    private IdentityListAdapter mIdentityListAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private SearchTask mSearchTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        setContentView(R.layout.activity_search);

        mSearchView = (SearchView) findViewById(R.id.search);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        mList = (ListView)getListView();

        mProgressViewAdapter = new ProgressViewAdapter(
                findViewById(R.id.search_progress),
                mList);

        mIdentityListAdapter = new IdentityListAdapter() {
            @Override
            protected LayoutInflater getLayoutInflater() {
                return WotSearchActivity.this.getLayoutInflater();
            }
        };
        setListAdapter(mIdentityListAdapter);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                // When user changed the Text: run search
                if (query != null && query.length() >= MIN_SEARCH_CHARACTERS) {
                    doSearch(query);
                }
                else {
                    mIdentityListAdapter.setItems(IdentityListAdapter.EMPTY_LIST);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                // When user changed the Text: run search
                if (query != null && query.length() >= MIN_SEARCH_CHARACTERS) {
                    doSearch(query);
                }
                else {
                    mIdentityListAdapter.setItems(IdentityListAdapter.EMPTY_LIST);
                }
                return true;
            }
        });

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }

        // Apply the choice mode (could be change by parameter)
        int choiceMode = intent.getIntExtra(PARAM_CHOICE_MODE, ListView.CHOICE_MODE_SINGLE);
        mList.setChoiceMode(choiceMode);

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

        Identity identity = (Identity)l.getAdapter().getItem(position);
        Log.d(TAG, "click on " + identity.getUid() +  "/" + DateUtils.format(identity.getTimestamp())+"/"+identity.getPubkey()  );

        // Open the identity
        try {
            Intent intent = new Intent(this, IdentityActivity.class);
            intent.putExtra(IdentityActivity.PARAM_IDENTITY, identity);
            startActivity(intent);
        }
        catch (Throwable t) {
            onError(t);
        }
    }

    protected void doSearch(String searchQuery) {

        if (mSearchTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid uid
        if (TextUtils.isEmpty(searchQuery)) {
            mIdentityListAdapter.setItems(IdentityListAdapter.EMPTY_LIST);
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
            mProgressViewAdapter.showProgress(true);
            mSearchTask = new SearchTask(searchQuery);
            mSearchTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class SearchTask extends AsyncTaskHandleException<Void, Void, List<Identity>> {

        private final String mSearchQuery;

        SearchTask(String mSearchQuery) {
            this.mSearchQuery = mSearchQuery;
        }

        @Override
        protected List<Identity> doInBackgroundHandleException(Void... params) {

            WotService service = ServiceLocator.instance().getWotService();
            WotLookupResults results = service.find(mSearchQuery);

            if (results == null) {
                return null;
            }

            return service.toIdentities(results);
        }

        @Override
        protected void onSuccess(List<Identity> identities) {
            mSearchTask = null;

            if (identities == null || identities.size() == 0) {
                // TODO NLS
                Toast.makeText(WotSearchActivity.this,
                        "No user found",
                        Toast.LENGTH_SHORT).show();
                mSearchView.requestFocus();
                mIdentityListAdapter.setItems(IdentityListAdapter.EMPTY_LIST);
            }
            else {
                mIdentityListAdapter.setItems(identities);
            }
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            Toast.makeText(WotSearchActivity.this,
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            mSearchView.requestFocus();
            mIdentityListAdapter.setItems(IdentityListAdapter.EMPTY_LIST);
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mSearchTask = null;
            mProgressViewAdapter.showProgress(false);
        }
    }

    protected void onError(Throwable t) {
        Toast.makeText(WotSearchActivity.this,
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();
        Log.e(TAG, t.getMessage());
    }
}
