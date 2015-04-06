package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.exception.PeerConnectionException;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.task.ProgressDialogAsyncTaskListener;
import io.ucoin.app.view.SlidingTabLayout;


public class HomeFragment extends Fragment {

    private View mStatusPanel;
    private TextView mStatusText;
    private ImageView mStatusImage;
    //private TabHost mTabs;
    private SlidingTabLayout mSlidingTabLayout;
    private MainActivity.QueryResultListener<Wallet> mWalletResultListener;

    public static HomeFragment newInstance() {
        return new HomeFragment();
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
        return inflater.inflate(R.layout.fragment_home,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tab host
        /*mTabs = (TabHost)view.findViewById(R.id.tabHost);
        mTabs.setup();
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator(getString(R.string.wallets));
            mTabs.addTab(spec);
        }
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator(getString(R.string.favorites));
            mTabs.addTab(spec);
        }*/

        mStatusPanel = view.findViewById(R.id.status_panel);
        mStatusPanel.setVisibility(View.GONE);

        // Currency text
        mStatusText = (TextView) view.findViewById(R.id.status_text);

        // Image
        mStatusImage = (ImageView) view.findViewById(R.id.status_image);

        /*
        // Tab 1: wallet list
        {
            WalletListFragment fragment1 = WalletListFragment.newInstance(
                    // Manage click on wallet
                    new WalletListFragment.OnClickListener() {
                        @Override
                        public void onPositiveClick(Bundle args) {
                            Wallet wallet = (Wallet) args.getSerializable(Wallet.class.getSimpleName());
                            onWalletClick(wallet);
                        }
                    });
            mWalletResultListener = fragment1;
            getFragmentManager().beginTransaction()
                    .replace(R.id.tab1, fragment1, "tab1")
                    .commit();
        }

        // Tab 2: contact list
        {
            ContactListFragment fragment2 = ContactListFragment.newInstance();
            getFragmentManager().beginTransaction()
                .replace(R.id.tab2, fragment2, "tab2")
                .commit();
        }*/

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager;
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new HomePagerAdapter(getChildFragmentManager()));

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(viewPager);

        // Load wallets
        LoadWalletsTask loadWalletsTask = new LoadWalletsTask();
        loadWalletsTask.execute();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle(getString(R.string.app_name));
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(false);
            ((IToolbarActivity) activity).setToolbarColor(getResources().getColor(R.color.primary));
        }
    }

    //Return false to allow normal menu processing to proceed, true to consume it here
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    protected void onWalletClick(final Wallet wallet) {
        Fragment fragment = WalletFragment.newInstance(wallet);
        FragmentManager fragmentManager = getFragmentManager();
        // Insert the Home at the first place in back stack
        fragmentManager.popBackStack(HomeFragment.class.getSimpleName(), 0);
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private class HomePagerAdapter extends FragmentPagerAdapter {

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link io.ucoin.app.view.SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return getString(R.string.wallets);
            else
                return getString(R.string.favorites);
        }

        @Override
        public android.app.Fragment getItem(int i) {

            android.app.Fragment fragment;
            if(i == 0) {
                fragment =  WalletListFragment.newInstance(
                        // Manage click on wallet
                        new WalletListFragment.OnClickListener() {
                            @Override
                            public void onPositiveClick(Bundle args) {
                                Wallet wallet = (Wallet) args.getSerializable(Wallet.class.getSimpleName());
                                onWalletClick(wallet);
                            }
                        });
                mWalletResultListener = (WalletListFragment)fragment;
                fragment.setHasOptionsMenu(true);
            }
            else {
                fragment = ContactListFragment.newInstance();
                fragment.setHasOptionsMenu(true);
            }

            return fragment;
        }
    }

    public class LoadWalletsTask extends AsyncTaskHandleException<Void, Void, List<Wallet>> {

        private long mAccountId;
        private Application mApplication;

        public LoadWalletsTask() {
            super(getActivity());

            mApplication = (Application)getActivity().getApplication();
            mAccountId = mApplication.getAccountId();

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            ProgressDialogAsyncTaskListener listener = new ProgressDialogAsyncTaskListener(progressDialog);
            setListener(listener);
        }

        @Override
        protected List<Wallet> doInBackgroundHandleException(Void... param) throws PeerConnectionException{
            ServiceLocator serviceLocator = ServiceLocator.instance();

            setMax(100);
            setProgress(0);

            // Load caches
            {
                setMessage(getString(R.string.starting_home));

                // Load currencies cache
                serviceLocator.getCurrencyService().loadCache(mApplication);
                increment();

                // Load peers cache
                serviceLocator.getPeerService().loadCache(mApplication);
                increment();
            }

            // Load wallets
            return serviceLocator.getWalletService().getWalletsByAccountId(
                    getContext(),
                    mAccountId,
                    true,
                    LoadWalletsTask.this);
        }

        @Override
        protected void onSuccess(final List<Wallet> wallets) {
            mWalletResultListener.onQuerySuccess(wallets);
        }

        @Override
        protected void onFailed(Throwable t) {
            final String errorMessage = getString(R.string.connected_error, t.getMessage());
            Log.e(getClass().getSimpleName(), errorMessage, t);

            mWalletResultListener.onQueryFailed(null);

            // Error when no network connection
            mStatusText.setText(getString(R.string.not_connected));
            mStatusImage.setImageResource(R.drawable.warning45);
            mStatusImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Display the error on click
                    Toast.makeText(getContext(),
                            getString(R.string.connected_error, errorMessage),
                            Toast.LENGTH_LONG)
                            .show();
                }
            });

            //ViewUtils.toogleViews(mTabs, mStatusPanel);
            ViewUtils.toogleViews(mSlidingTabLayout, mStatusPanel);

            // Display the error
            Toast.makeText(getContext(),
                    errorMessage,
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
