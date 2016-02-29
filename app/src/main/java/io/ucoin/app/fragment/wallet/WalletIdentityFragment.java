package io.ucoin.app.fragment.wallet;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.activity.CurrencyActivity;
import io.ucoin.app.fragment.identity.MemberListFragment;
import io.ucoin.app.fragment.identity.MembershipListFragment;
import io.ucoin.app.fragment.identity.SelfCertificationListFragment;
import io.ucoin.app.model.UcoinContact;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.sql.sqlite.Contacts;
import io.ucoin.app.model.sql.sqlite.Currency;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;
import io.ucoin.app.widget.SlidingTabLayout;

public class WalletIdentityFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;

    private Cursor mCursor;
    private LinearLayout mHeaderLayout;
    private TextView mUid;
    private UcoinContact mContact;

    public static String WALLET_ID = "wallet_id";

    public static WalletIdentityFragment newInstance(Bundle args) {
        WalletIdentityFragment fragment = new WalletIdentityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_wallet_identity,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(getString(R.string.identity));
        ((CurrencyActivity) getActivity()).setDrawerIndicatorEnabled(false);

        mHeaderLayout = (LinearLayout) getView().findViewById(R.id.header);
        mViewPager = (ViewPager) getView().findViewById(R.id.viewpager);
        mSlidingTabLayout = (SlidingTabLayout) getView().findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);

        mUid = (TextView) view.findViewById(R.id.uid);

        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_identity, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteItem = menu.findItem(R.id.action_delete);

        if (mContact==null) {
            deleteItem.setVisible(false);
        } else {
            deleteItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                actionDelete();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        String publicKey = args.getString(Application.IDENTITY_PUBLICKEY);
//        String uid = args.getString(Application.IDENTITY_UID);
        Long walletId = args.getLong(Application.IDENTITY_WALLET_ID);
//        Long currencyId = args.getLong(Application.IDENTITY_CURRENCY_ID);
        //UcoinCurrency currency = new Currency(getActivity(), getArguments().getLong(BaseColumns._ID));
//        UcoinWallet wallet = new Wallets(getActivity(),currencyId).getById(walletId);
//        try {
//            UcoinIdentity identity = wallet.addIdentity(uid, publicKey);
//        } catch (AddressFormatException e) {
//            e.printStackTrace();
//        }
        String selection = SQLiteTable.Identity.WALLET_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(walletId)};
        return new CursorLoader(
                getActivity(),
                UcoinUris.IDENTITY_URI,
                null, selection, selectionArgs,
                BaseColumns._ID + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mCursor = data;
            String uid = data.getString(data.getColumnIndex(SQLiteView.Identity.UID));
            Long currencyId = data.getLong(data.getColumnIndex(SQLiteView.Identity.CURRENCY_ID));
            String publicKey = data.getString(data.getColumnIndex(SQLiteView.Identity.PUBLIC_KEY));
            mContact = new Contacts(getActivity(),currencyId).getByPublicKey(publicKey);
            if(mContact!=null){
                mUid.setText(mContact.name().concat(" (").concat(mContact.uid()).concat(")"));
            }else{
                mUid.setText(uid);
            }
            mHeaderLayout.setVisibility(View.VISIBLE);
            mSlidingTabLayout.setVisibility(View.VISIBLE);

            // Get the ViewPager and set it's PagerAdapter so that it can display items
            if (mViewPager.getAdapter() == null) {
                mViewPager.setAdapter(new IdentityPagerAdapter(getChildFragmentManager()));

                // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
                // it's PagerAdapter set.
                mSlidingTabLayout.setViewPager(mViewPager);
            }

        } else {
            mViewPager.setAdapter(null);
            mSlidingTabLayout.setViewPager(null);

            mHeaderLayout.setVisibility(View.GONE);
            mSlidingTabLayout.setVisibility(View.GONE);

            mUid.setText("");
        }


        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public void actionDelete() {
        UcoinCurrency currency = new Currency(getActivity(), getArguments().getLong(BaseColumns._ID));
        currency.identity().delete();
    }

    public void actionRevoke() {
        /*
        UcoinIdentity identity = getArguments().getParcelable(UcoinIdentity.class.getSimpleName());
        identity.setSync(IdentityState.SEND_REVOKE);
        Application.requestSync(getActivity());
        */
    }

    private class IdentityPagerAdapter extends FragmentStatePagerAdapter {

        public IdentityPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link io.ucoin.app.widget.SlidingTabLayout}.
         * <p/>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return getString(R.string.certification);
            else if (position == 1)
                return getString(R.string.membership);
            else
                return getString(R.string.self);
        }

        @Override
        public android.app.Fragment getItem(int i) {
            android.app.Fragment fragment;

            if (i == 0) {
                fragment = MemberListFragment.newInstance(
                        getArguments().getLong(BaseColumns._ID),
                        mCursor.getLong(mCursor.getColumnIndex(SQLiteView.Identity._ID)));
                fragment.setHasOptionsMenu(true);
            } else if (i == 1) {
                fragment = MembershipListFragment.newInstance(mCursor.getLong(mCursor.getColumnIndex(SQLiteView.Identity._ID)));
                fragment.setHasOptionsMenu(true);
            } else {
                fragment = SelfCertificationListFragment.newInstance(mCursor.getLong(mCursor.getColumnIndex(SQLiteView.Identity._ID)));
                fragment.setHasOptionsMenu(true);
            }
            return fragment;
        }
    }
}
