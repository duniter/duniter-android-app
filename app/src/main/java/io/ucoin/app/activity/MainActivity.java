package io.ucoin.app.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.adapter.DrawerCurrencyCursorAdapter;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.dao.sqlite.SQLiteTable;
import io.ucoin.app.content.Provider;
import io.ucoin.app.fragment.common.HomeFragment;
import io.ucoin.app.fragment.contact.ContactListFragment;
import io.ucoin.app.fragment.currency.AddCurrencyDialogFragment;
import io.ucoin.app.fragment.currency.CurrencyFragment;
import io.ucoin.app.fragment.wallet.TransferFragment;
import io.ucoin.app.fragment.wot.IdentityFragment;
import io.ucoin.app.fragment.wot.WotSearchFragment;
import io.ucoin.app.model.local.Contact;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.task.AddCurrencyTask;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.CurrencyUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class MainActivity extends ActionBarActivity
        implements ListView.OnItemClickListener,
        IToolbarActivity,
        LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MIN_SEARCH_CHARACTERS = 2;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private QueryResultListener<Identity> mQueryResultListener;
    private ListView mDrawerListView;
    private TextView mDrawerEmptyListView;

    private Toolbar mToolbar;
    private boolean mUnitPreferenceChanged = false;

    //private RemoteServiceConnection mRemoteServiceConnection;
    //private RemoteServiceLocator mRemoteServiceLocator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare some utilities
        //Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        DateUtils.setDefaultMediumDateFormat(getMediumDateFormat());
        DateUtils.setDefaultLongDateFormat(getLongDateFormat());
        DateUtils.setDefaultShortDateFormat(getShortDateFormat());
        DateUtils.setDefaultTimeFormat(getTimeFormat());
        CurrencyUtils.setDefaultLocale(getResources().getConfiguration().locale);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);

        // Init configuration
        Configuration config = new Configuration();
        Configuration.setInstance(config);

        // Load account
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(getString(R.string.ACCOUNT_TYPE));


        /* TODO : create the servcie connection
        mRemoteServiceConnection = new RemoteServiceConnection() {
            @Override
            public void onServiceLoaded(RemoteServiceLocator service) {
                mRemoteServiceLocator = service;
            }
        };*/

        // If first time: create account
        if (accounts.length == 0) {
            Intent intent = new Intent(this, AddAccountActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        //todo handle this case
        Account account = loadLastAccountUsed(accountManager, accounts);
        if (account == null) {
            Toast.makeText(this, "Could Not load account", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Set toolbar as action bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (getSupportActionBar() == null
                && mToolbar != null) {
            try {
                setSupportActionBar(mToolbar);
            } catch (Throwable t) {
                Log.w("setSupportActionBar", t.getMessage());
            }
        }

        //Navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Set the adapter for the drawer list view
        DrawerCurrencyCursorAdapter drawerCurrencyCursorAdapter
                = new DrawerCurrencyCursorAdapter(this, null, 0);
        mDrawerListView = (ListView) findViewById(R.id.drawer_listview);
        mDrawerListView.setAdapter(drawerCurrencyCursorAdapter);
        mDrawerListView.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);

        mDrawerEmptyListView = (TextView) findViewById(R.id.drawer_empty_list);

        //Navigation drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout
                , R.string.open_drawer, R.string.close_drawer);
        TextView addCurrency = (TextView) findViewById(R.id.drawer_add_currency);

        addCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment fragment = AddCurrencyDialogFragment.newInstance(new AddCurrencyDialogFragment.OnClickListener() {
                    @Override
                    public void onPositiveClick(Bundle args) {
                        Peer peer = (Peer) args.getSerializable(Peer.class.getSimpleName());
                        AddCurrencyTask task = new AddCurrencyTask(getApplicationContext(), true/*with progress*/);
                        task.execute(peer);
                    }
                });
                fragment.show(getFragmentManager(),
                        fragment.getClass().getSimpleName());
            }
        });

        // Home
        TextView drawerHome = (TextView) findViewById(R.id.drawer_home);
        drawerHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHomeFragment();
            }
        });

        // Settings
        TextView drawerSettings = (TextView) findViewById(R.id.drawer_settings);
        drawerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Help (open ucoin forum)
        TextView drawerHelp = (TextView) findViewById(R.id.drawer_help);
        drawerHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri forumUri = Uri.parse(Configuration.instance().getForumUrl());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, forumUri);
                startActivity(browserIntent);
            }
        });

        // Contact drawer
        {
            ContactListFragment fragment = ContactListFragment.newInstance(
                    // Manage click on contact
                    new ContactListFragment.ContactListListener() {
                        @Override
                        public void onPositiveClick(Bundle args) {
                            Long contactId = args.getLong(ContactListFragment.BUNDLE_CONTACT_ID);
                            openContact(contactId);
                        }
                    });
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_contact_list, fragment, fragment.getClass().getSimpleName())
                    .commit();
        }

        ContentResolver.setSyncAutomatically(account, getString(R.string.AUTHORITY), true);

        // Open the default fragment
        openDefaultFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // TODO : Bound remote service
        /*
        {
            Intent intent = new Intent(this, RemoteServiceLocator.class);
            bindService(intent, mRemoteServiceConnection, Context.BIND_AUTO_CREATE);
        }*/

    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO Unbind the remote service
        /*if (mRemoteServiceConnection.isBound()){
            unbindService(mRemoteServiceConnection);
        }*/
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        //todo handle screen orientation change
        //for now it is just discarded by adding
        //android:configChanges="orientation|screenSize" in the manifest
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * This method will detect when a change on pref should restart the main activity
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SettingsActivity.PREF_UNIT)) {
            mUnitPreferenceChanged = true;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mUnitPreferenceChanged) {
            openHomeFragment();
            mUnitPreferenceChanged = false;
        }
    }

    @Override
    protected void onDestroy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
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
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_contact_list:
                openContactDrawer();
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            closeDrawer();
            return;
        }

        int bsEntryCount = getFragmentManager().getBackStackEntryCount();
        if (bsEntryCount <= 1) {
            super.onBackPressed();
            return;
        }

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

        getFragmentManager().popBackStack();
    }

    public boolean onQueryTextSubmit(MenuItem searchItem, String query) {

        searchItem.getActionView().clearFocus();
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.frame_content);
        boolean isWotFragmentExists = fragment == mQueryResultListener;

        // If fragment already visible, just refresh the arguments (to update title)
        if (!isWotFragmentExists) {
            fragment = WotSearchFragment.newInstance(query);
            mQueryResultListener = (WotSearchFragment)fragment;
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
        else {
            WotSearchFragment.setArguments((WotSearchFragment) fragment, query);
        }

        if (query.length() >= MIN_SEARCH_CHARACTERS) {
            SearchTask searchTask = new SearchTask();
            searchTask.execute(query);
        }
        else {
            mQueryResultListener.onQueryFailed(getString(R.string.query_too_short, MIN_SEARCH_CHARACTERS));
        }

        return true;
    }

    // nav drawer currency items click
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Currency currency = ServiceLocator.instance().getCurrencyService().getCurrencyById(this, id);
        Fragment fragment = CurrencyFragment.newInstance(currency);
        reloadFirstFragment(fragment);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = SQLiteTable.Currency.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                ((Application) getApplication()).getAccountIdAsString()
        };

        // Create the currencies loader, using cursor
        return new CursorLoader(
                this,
                Provider.CURRENCY_URI,
                null,
                selection,
                selectionArgs,
                SQLiteTable.Currency.NAME + " ASC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0) {
            mDrawerEmptyListView.setVisibility(View.VISIBLE);
            mDrawerListView.setVisibility(View.GONE);
        } else {
            mDrawerEmptyListView.setVisibility(View.GONE);
            mDrawerListView.setVisibility(View.VISIBLE  );
        }
        ((DrawerCurrencyCursorAdapter) mDrawerListView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("MAINACTIVITY", "onLoaderReset");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setToolbarDrawable(Drawable drawable) {
        // On Jelly Bean we have the setBackground(Drawable) APIs
        // If available, use these APIs to change the toolbar background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mToolbar.setBackground(drawable);
        }
    }

    /**
     * Display an an arrow in the toolbar to get to the previous fragment
     * or an hamburger icon to open the navigation drawer
     */
    @Override
    public void setToolbarBackButtonEnabled(boolean enabled) {
        // Show back button
        if (enabled) {
            // Show the back button (= Home button in the action bar)
            getSupportActionBar().setHomeButtonEnabled(true);

            // Hide the drawer toggle button
            mDrawerToggle.setDrawerIndicatorEnabled(false);

            // Set icon
            getSupportActionBar().setIcon(null);

        }

        // Hide the back button
        else {
            getSupportActionBar().setHomeButtonEnabled(false);

            // Show the drawer toggle button
            mDrawerToggle.setDrawerIndicatorEnabled(true);

            getSupportActionBar().setIcon(R.drawable.ic_ucoin);
        }

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

    protected DateFormat getShortDateFormat() {
        return android.text.format.DateFormat.getDateFormat(getApplicationContext());
    }

    protected DateFormat getTimeFormat() {
        return android.text.format.DateFormat.getTimeFormat(getApplicationContext());
    }

    protected void openDefaultFragment() {
        Intent intent = getIntent();

        // Open transfer if a given URI exists
        if (intent != null && intent.getAction() == Intent.ACTION_VIEW) {
            Uri uri = intent.getData();
            Log.d("MAINACTIVITY", "Asking to open uri: " + uri.toString());

            Identity identity = new Identity();
            List<String> pathSegments = uri.getPathSegments();

            if (pathSegments.size()== 2) {
                identity.setCurrency(pathSegments.get(0));
                identity.setPubkey(pathSegments.get(1));

                openTransfertFragment(identity);
                return;
            }
        }

        // Open the home screen
        openHomeFragment();

        // Init app (caches) in background thread
        new InitTask().execute();
    }

    protected void openHomeFragment() {

        /*Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);
        if (fragment != null && fragment instanceof HomeFragment) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
            getFragmentManager().popBackStack()
        }*/

        Fragment fragment = HomeFragment.newInstance();
        reloadFirstFragment(fragment);
    }

    protected void openTransfertFragment(Identity identity) {

        Fragment fragment = getFragmentManager().findFragmentById(R.id.frame_content);
        if (fragment != null && fragment instanceof TransferFragment) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
            getFragmentManager().popBackStack();
        }


        fragment = TransferFragment.newInstance(identity);

        getFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.fade_in,
                        R.animator.fade_out)
                .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public Account loadLastAccountUsed(AccountManager accountManager, Account[] accounts) {

        for (Account account : accounts) {
            String account_id = accountManager.getUserData(account, "_id");

            String last_account_id = getSharedPreferences("account", MODE_PRIVATE)
                    .getString("_id", "");

            if (last_account_id.equals(account_id)) {
                // Init the account to use, and init the data loader,
                ((Application) getApplication()).setAccount(account);
                this.getLoaderManager().initLoader(0, null, this);
                return account;
            }
        }

        return null;
    }

    public void reloadFirstFragment(Fragment fragment) {

        //replace fragment
        FragmentManager fragmentManager = getFragmentManager();

        // Insert the fragment by replacing any existing fragment
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        // close the drawer
        closeDrawer();
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(findViewById(R.id.drawer_panel));
    }

    protected void openContactDrawer() {
        mDrawerLayout.openDrawer(findViewById(R.id.contact_drawer_panel));
    }

    public void closeContactDrawer() {
        mDrawerLayout.closeDrawer(findViewById(R.id.contact_drawer_panel));
    }



    protected void openContact(final Long contactId) {
        if (contactId == null) {
            return;
        }

        // Load contact
        Contact contact = ServiceLocator.instance().getContactService().getContactViewById(getApplicationContext(), contactId);
        if (contact == null || CollectionUtils.isEmpty(contact.getIdentities())) {
            return;
        }

        Identity identity = null;
        if (contact.getIdentities().size() == 1) {
            identity = contact.getIdentities().get(0);
        }
        else {
            // TODO : open a dialog with multi choice ?
            // (contact with multi-identities/currency)
        }

        if (identity != null) {

            Fragment fragment = IdentityFragment.newInstance(identity);
            FragmentManager fragmentManager = getFragmentManager();
            // Insert the Home at the first place in back stack
            fragmentManager.beginTransaction()
                    .setCustomAnimations(
                            R.animator.delayed_slide_in_up,
                            R.animator.fade_out,
                            R.animator.delayed_fade_in,
                            R.animator.slide_out_up)
                    .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();

            closeContactDrawer();
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


    public interface QueryResultListener<T> {
        public void onQuerySuccess(List<? extends T> identities);

        public void onQueryFailed(String message);

        public void onQueryCancelled();
    }

    /**
     * Initialize the app (load caches)
     */
    public class InitTask extends AsyncTaskHandleException<Void, Void, Void> {

        private final long mAccountId;

        public InitTask() {
            super(MainActivity.this.getApplicationContext());
            mAccountId = ((io.ucoin.app.Application)getApplication()).getAccountId();
        }

        @Override
        protected Void doInBackgroundHandleException(Void... params) throws Exception {
            ServiceLocator.instance().loadCaches(getContext(), mAccountId);
            return null;
        }
    }

    public class SearchTask extends AsyncTaskHandleException<String, Void, List<Identity>> {

        public SearchTask() {
            super(MainActivity.this);
        }

        @Override
        protected List<Identity> doInBackgroundHandleException(String... queries) throws PeerConnectionException {

            // Get list of currencies
            Set<Long> currenciesIds = ServiceLocator.instance().getCurrencyService().getCurrencyIds();

            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();
            List<Identity> results = service.findIdentities(currenciesIds, queries[0]);

            if (results == null) {
                return null;
            }

            return results;
        }

        @Override
        protected void onSuccess(List<Identity> identities) {
            mQueryResultListener.onQuerySuccess(identities);
        }

        @Override
        protected void onFailed(Throwable t) {
            mQueryResultListener.onQueryFailed(ExceptionUtils.getMessage(t));
        }

        @Override
        protected void onCancelled() {
            mQueryResultListener.onQueryCancelled();
        }
    }


}