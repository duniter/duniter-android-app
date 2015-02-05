package io.ucoin.app.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.fragment.CommunityListFragment;
import io.ucoin.app.fragment.DevFragment;
import io.ucoin.app.fragment.HomeFragment;
import io.ucoin.app.fragment.LoginFragment;
import io.ucoin.app.fragment.TransferListFragment;
import io.ucoin.app.fragment.WotSearchFragment;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.WotLookupResults;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.WotService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.DateUtils;


public class MainActivity extends ActionBarActivity
        implements ListView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MIN_SEARCH_CHARACTERS = 2;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private QueryResultListener mQueryResultListener;

    private TextView mUidView;
    private TextView mPubkeyView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LOAD account
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

        if (accounts.length == 0) {
            Intent intent = new Intent(this, AddAccountActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //todo handle this case
        Account account = loadLastAccountUsed();
        if (account == null) {
            Toast.makeText(this, "Could Not load account", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        // Prepare some utilities
        //Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        DateUtils.setDefaultMediumDateFormat(getMediumDateFormat());
        DateUtils.setDefaultLongDateFormat(getLongDateFormat());

        setContentView(R.layout.activity_main);

        // Init configuration
        Configuration config = new Configuration();
        Configuration.setInstance(config);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        try {
            setSupportActionBar(mToolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }

        //Navigation drawer
        View listHeader = getLayoutInflater().inflate(R.layout.drawer_header, null);
        mUidView = (TextView) listHeader.findViewById(R.id.uid);
        mPubkeyView = (TextView) listHeader.findViewById(R.id.public_key);

        String[] drawerListItems = getResources().getStringArray(R.array.drawer_items);
        ListView drawerListView = (ListView) findViewById(R.id.drawer_listview);

        drawerListView.addHeaderView(listHeader);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Set the adapter for the drawer list view
        drawerListView.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, drawerListItems));

        drawerListView.setOnItemClickListener(this);
        //Navigation drawer toggle
        //Please use ActionBarDrawerToggle(Activity, DrawerLayout, int, int)
        // if you are setting the Toolbar as the ActionBar of your activity.
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout
                , R.string.open_drawer, R.string.close_drawer);


        ContentResolver.setSyncAutomatically(account, getString(R.string.AUTHORITY), true);

        Fragment fragment;
        fragment = HomeFragment.newInstance();

        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.fade_in,
                        R.animator.fade_out)
                .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        //todo handle screen orientation change
        //for now it is just discarded by adding
        //android:configChanges="orientation|screenSize" in the manifest
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    //Called once during the whole activity lifecycle
    // after the first onResume() call
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }

        int bsEntryCount = getFragmentManager().getBackStackEntryCount();
        String currentFragment = getFragmentManager()
                .getBackStackEntryAt(bsEntryCount - 1)
                .getName();

        Fragment fragment = getFragmentManager().findFragmentByTag(currentFragment);

        //fragment that need to handle onBackPressed
        //shoud implements MainActivity.OnBackPressedInterface
        if(fragment instanceof OnBackPressed) {
            if(((OnBackPressed) fragment).onBackPressed()) {
                return;
            }
        }


        if (getFragmentManager().getBackStackEntryCount() == 1) {
            //leave the activity
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public boolean onQueryTextSubmit(MenuItem searchItem, String query) {

        searchItem.getActionView().clearFocus();
        WotSearchFragment fragment = WotSearchFragment.newInstance(query);
        mQueryResultListener = fragment;
        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        if (query.length() > MIN_SEARCH_CHARACTERS) {
            SearchTask searchTask = new SearchTask(query);
            searchTask.execute((Void) null);
        }

        return true;
    }

    // nav drawer items
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Fragment fragment = null;
        switch (position) {
            case 1: //0 is home we only pop back, no need for new fragment
                break;
            case 2:
                fragment = CommunityListFragment.newInstance();
                break;
            case 3:
                fragment = LoginFragment.newInstance();
                break;
            case 4:
                fragment = TransferListFragment.newInstance();
                break;
            case 5:
                Intent intent = new Intent(MainActivity.this,
                        SettingsActivity.class);
                startActivity(intent);
                break;
            case 6:
                fragment = DevFragment.newInstance();
                break;
            default:

        }

        //replace fragment
        FragmentManager fragmentManager = getFragmentManager();
        if (fragment == null) {
            fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        } else {
            // Insert the fragment by replacing any existing fragment
            fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(
                            R.animator.delayed_fade_in,
                            R.animator.fade_out,
                            R.animator.delayed_fade_in,
                            R.animator.fade_out)
                    .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }

        // close the drawer
        mDrawerLayout.closeDrawer(findViewById(R.id.drawer_listview));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String account_id = ((Application) getApplication()).getAccountId();
        Uri uri = Uri.parse(Provider.CONTENT_URI + "/account/" + account_id);

        return new CursorLoader(this, uri, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        int uidIndex = data.getColumnIndex(Contract.Account.UID);
        int pubkeyIndex = data.getColumnIndex(Contract.Account.PUBLIC_KEY);

        while (data.moveToNext()) {
            mUidView.setText(data.getString(uidIndex));
            mPubkeyView.setText(data.getString(pubkeyIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("MAINACTIVITY", "onLoaderReset");
    }

    public void setToolbarColor(int colorRes) {
        mToolbar.setBackgroundColor(colorRes);
    }

    /* -- Internal methods -- */
    protected DateFormat getMediumDateFormat() {
        final String format = Settings.System.getString(getContentResolver(), Settings.System.DATE_FORMAT);
        if (TextUtils.isEmpty(format)) {
            return android.text.format.DateFormat.getMediumDateFormat(getApplicationContext());
        } else {
            return new SimpleDateFormat(format);
        }
    }

    protected DateFormat getLongDateFormat() {
        return android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
    }

    public Account loadLastAccountUsed() {
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

        for (Account account : accounts) {
            String account_id = accountManager.getUserData(account, "_id");

            String last_account_id = getSharedPreferences("account", MODE_PRIVATE)
                    .getString("_id", "");

            if (last_account_id.equals(account_id)) {
                ((Application) getApplication()).setAccount(account);
                this.getLoaderManager().initLoader(0, null, this);
                return account;
            }
        }
        finish();
        return null;
    }

    /**
     * Display an an arrow in the toolbar to get to the previous fragment
     * or an hamburger icon to open the navigation drawer
     */
    public void setBackButtonEnabled(boolean enabled) {
        if (enabled) {
            mToggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        } else {
            mToggle.setDrawerIndicatorEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

    }

    /**
     * Interface for handling OnBackPressed event in fragments     *
     */
    public interface OnBackPressed {
        /**
         *
         * @return true if the events has been handled, false otherwise
         */
        public boolean onBackPressed();
    }

    public interface QueryResultListener {
        public void onQuerySuccess(List<Identity> identities);

        public void onQueryFailed();

        public void onQueryCancelled();
    }

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
            mQueryResultListener.onQuerySuccess(identities);
        }

        @Override
        protected void onFailed(Throwable t) {
            mQueryResultListener.onQueryFailed();
        }

        @Override
        protected void onCancelled() {
            mQueryResultListener.onQueryCancelled();
        }
    }

}