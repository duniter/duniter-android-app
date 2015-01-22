package io.ucoin.app.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.fragment.DevFragment;
import io.ucoin.app.fragment.HomeFragment;
import io.ucoin.app.fragment.LoginFragment;
import io.ucoin.app.fragment.WotSearchFragment;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.WotLookupResults;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.WotService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.DateUtils;


public class MainActivity extends ActionBarActivity
        implements ListView.OnItemClickListener {
    private static final int MIN_SEARCH_CHARACTERS = 2;
    private ActionBarDrawerToggle mToggle;
    private DrawerLayout mDrawerLayout;
    private QueryResultListener mQueryResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //invalidateOptionsMenu();

        // Prepare some utilities
        //Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
        DateUtils.setDefaultMediumDateFormat(getMediumDateFormat());
        DateUtils.setDefaultLongDateFormat(getLongDateFormat());

        setContentView(R.layout.activity_main);

        // Init configuration
        Configuration config = new Configuration();
        Configuration.setInstance(config);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        try {
            setSupportActionBar(toolbar);
        } catch (Throwable t) {
            Log.w("setSupportActionBar", t.getMessage());
        }

        //Navigation drawer
        String[] drawerListItems = getResources().getStringArray(R.array.drawer_items);
        ListView drawerListView = (ListView) findViewById(R.id.drawer_listview);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Set the adapter for the drawer list view
        drawerListView.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, drawerListItems));

        drawerListView.setOnItemClickListener(this);

        //Navigation drawer toggle
        //Please use ActionBarDrawerToggle(Activity, DrawerLayout, int, int) if you are setting the Toolbar as the ActionBar of your activity.
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout
                , R.string.open_drawer, R.string.close_drawer);


        //Initial fragment
        Fragment fragment = new HomeFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
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
        //MainActivity only handles the navigation drawer
        //The menu items specific to each fragment
        //are handled in their respective
        // fragment.onCreateOptionsMenu()
        FragmentManager fm = getFragmentManager();

        int backstackEntryCount = fm.getBackStackEntryCount();
        String currentFragmentName = fm.getBackStackEntryAt(backstackEntryCount - 1).getName();

        //Fragments for which the menu "hamburger" icon is displayed
        //These should  be the same fragment as those linked in the nav drawer
        if (currentFragmentName.equals(HomeFragment.class.getSimpleName()) ||
                currentFragmentName.equals(DevFragment.class.getSimpleName()) ||
                currentFragmentName.equals(LoginFragment.class.getSimpleName())) {
            mToggle.setDrawerIndicatorEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(false);
        } else {
            mToggle.setDrawerIndicatorEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

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

        FragmentManager fm = getFragmentManager();

        int backstackEntryCount = fm.getBackStackEntryCount();
        String currentFragmentName = fm.getBackStackEntryAt(backstackEntryCount - 1).getName();

        if (currentFragmentName.equals(HomeFragment.class.getSimpleName())) {
            //leave the activity
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public boolean onQueryTextSubmit(MenuItem searchItem, String query) {
        WotSearchFragment fragment = WotSearchFragment.newInstance(query);
        mQueryResultListener = fragment;
        searchItem.getActionView().clearFocus();
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        if (query.length() > MIN_SEARCH_CHARACTERS) {
            SearchTask searchTask = new SearchTask(query);
            searchTask.execute((Void) null);
        }

        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //prepare fragment
        Fragment fragment = null;
        switch (position) {
            case 0: //0 is home we only pop back, no need for new fragment
                break;
            case 1:
                fragment = new LoginFragment();
                break;
            case 2:
                fragment = DevFragment.newInstance();
                break;
            case 3:
                Intent intent = new Intent(MainActivity.this,
                        SettingsActivity.class);
                startActivity(intent);
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
                    .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        }

        // close the drawer
        mDrawerLayout.closeDrawer(findViewById(R.id.drawer_listview));
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