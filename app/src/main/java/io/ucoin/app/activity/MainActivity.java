package io.ucoin.app.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.exception.UncaughtExceptionHandler;
import io.ucoin.app.fragment.CryptoTestFragment;
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

    private SearchView mSearchView;
    private MenuItem mSearchItem;

    private String[] mDrawerListItems;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    //Keep track of the login task to ensure we can cancel it if requested.
    private SearchTask mSearchTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.i("BACKSTACK", String.valueOf(getFragmentManager().getBackStackEntryCount()));
            }
        });

        // Prepare some utilities
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
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
        }
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        Fragment homeFragment = new HomeFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.frame_content, homeFragment, "HOME")
                .addToBackStack("HOME_BACKSTACK")
                .commit();

        mDrawerListItems = getResources().getStringArray(R.array.drawer_items);
        mDrawerListView = (ListView) findViewById(R.id.drawer_listview);
        mDrawerLayout =(DrawerLayout) findViewById(R.id.drawer_layout);
        // Set the adapter for the list view
        mDrawerListView.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mDrawerListItems));
        // Set the list's click listener
        mDrawerListView.setOnItemClickListener(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchItem = menu.findItem(R.id.action_wot_search);

        MenuItemCompat.setOnActionExpandListener(mSearchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_wot_search :
                        FragmentManager fragmentManager = getFragmentManager();
                        Fragment fragment = fragmentManager.findFragmentByTag("SEARCH");
                        //search frag does not exists yet create and replace
                        if (fragment == null)
                        {
                            fragment = new WotSearchFragment();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.frame_content, fragment, "SEARCH")
                                    .addToBackStack(null)
                                    .commit();
                        }
                        //search frag already exists it is in backstack, pop it if not visible.
                        else
                        {

                            if (!fragment.isVisible()) {
                                fragmentManager.popBackStack("SEARCH_BACKSTACK",
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        }
                        break;
                }

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (mSearchView.getQuery().length() > 0 ) {
                    setTitle("/" + mSearchView.getQuery());
                }
                return true;
            }
        });

        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchItem);
        // Assumes current activity is the searchable activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(true);

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                doSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                //hide the keyboard and remove focus on search view
                InputMethodManager inputManager =
                        (InputMethodManager) getApplicationContext().
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(
                        MainActivity.this.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
                mSearchView.clearFocus();
                return true;
            }
        });

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_wot_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    /****
     * Handles click on navigation drawer list item
     */
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {

        Fragment fragment = null;
        switch (position) {
            case 0: //0 is home we only pop back, no need for new fragment
                break;
            case 1:
                fragment = new LoginFragment();
                break;
            case 2:
                fragment = new CryptoTestFragment();
                break;
        }


        FragmentManager fragmentManager = getFragmentManager();
        if (fragment == null) {
            fragmentManager.popBackStack("HOME_BACKSTACK", 0);
        }
        else {
            // Insert the fragment by replacing any existing fragment
            fragmentManager.popBackStack("HOME_BACKSTACK", 0);
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_content, fragment)
                    .addToBackStack(null)
                    .commit();
        }
        // close the drawer
        mDrawerLayout.closeDrawer(findViewById(R.id.drawer_listview));
        if (mSearchItem.isActionViewExpanded()) {
            MenuItemCompat.collapseActionView(mSearchItem);
        }
    }

    @Override
    public void onBackPressed() {


        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }


        if (mSearchItem.isActionViewExpanded()){
            super.onBackPressed();
        }
        // if entrycount == 1 where are on home fragment
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
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
    
    protected void doSearch(String searchQuery) {

        if (mSearchTask != null) {
            return;
        }

        if (TextUtils.isEmpty(searchQuery) ||
                searchQuery.length() <= MIN_SEARCH_CHARACTERS) {
            mSearchView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            //mProgressViewAdapter.showProgress(true);
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
                Toast.makeText(MainActivity.this,
                        "No user found",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                updateSearchResult(identities);
            }
            //mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            Toast.makeText(MainActivity.this,
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            mSearchView.requestFocus();
            //mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mSearchTask = null;
            //mProgressViewAdapter.showProgress(false);
        }
    }
    
    public void updateSearchResult(List<Identity> list)
    {
        WotSearchFragment fragment = (WotSearchFragment)getFragmentManager()
                .findFragmentByTag("SEARCH");

         if(fragment != null) {
             fragment.callbackNewResult(list);
         }
    }

    public boolean collapseSearchView() {
        return MenuItemCompat.collapseActionView(mSearchItem);
    }
}
