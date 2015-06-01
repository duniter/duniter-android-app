package io.ucoin.app.fragment.currency;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.database.Contract;
import io.ucoin.app.database.Provider;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.technical.view.SlidingTabLayout;

public class CurrencyFragment extends Fragment {

    private SlidingTabLayout mSlidingTabLayout;

    public static CurrencyFragment newInstance(Currency currency) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Currency.class.getSimpleName(), currency);

        CurrencyFragment fragment = new CurrencyFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
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

        return inflater.inflate(R.layout.fragment_currency,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency.class.getSimpleName());

        TextView currencyName = (TextView) view.findViewById(R.id.currency);
        currencyName.setText(currency.getCurrencyName());

        TextView memberCount = (TextView) view.findViewById(R.id.members_count);
        memberCount.setText(getString(R.string.members_count, currency.getMembersCount()));

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager;
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(new HomePagerAdapter(getChildFragmentManager(), currency));

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(viewPager);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_currency, menu);

        //hide Add button and show delete if the currency first block signature
        // is already in the database
        Bundle newInstanceArgs = getArguments();
        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency.class.getSimpleName());

        String selection = Contract.Currency.ACCOUNT_ID + "=? AND " +
                Contract.Currency.FIRST_BLOCK_SIGNATURE + "=?";
        String[] selectionArgs = new String[]{
                ((Application) getActivity().getApplication()).getAccountIdAsString(),
                currency.getFirstBlockSignature()
        };
        Uri uri = Uri.parse(Provider.CONTENT_URI + "/currency/");
        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, null);

        //todo handle join action button
        if (cursor.getCount() > 0) {
            menu.removeItem(R.id.action_add);
        } else {
            menu.removeItem(R.id.action_delete);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle(R.string.currency);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
            ((IToolbarActivity) activity).setToolbarColor(getResources().getColor(R.color.primary));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                add();
                return true;
            case R.id.action_join:
                return true;
            case R.id.action_delete:
                delete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void add() {
        Bundle newInstanceArgs = getArguments();

        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency.class.getSimpleName());

        Peer[] peers = currency.getPeers();

        Long accountId = ((Application) getActivity().getApplication()).getAccountId();

        //add Currency to database
        ContentValues values = new ContentValues();
        values.put(Contract.Currency.ACCOUNT_ID, accountId);
        values.put(Contract.Currency.NAME, currency.getCurrencyName());
        values.put(Contract.Currency.MEMBERS_COUNT, currency.getMembersCount());
        values.put(Contract.Currency.FIRST_BLOCK_SIGNATURE, currency.getFirstBlockSignature());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/currency/");
        uri = getActivity().getContentResolver().insert(uri, values);

        //add Peer to database
        Peer peer = peers[0];

        if (peer != null) {
            Long id = ContentUris.parseId(uri);
            values = new ContentValues();
            values.put(Contract.Peer.CURRENCY_ID, Long.toString(id));
            values.put(Contract.Peer.HOST, peer.getHost());
            values.put(Contract.Peer.PORT, Integer.toString(peer.getPort()));
            uri = Uri.parse(Provider.CONTENT_URI + "/peer/");
            uri = getActivity().getContentResolver().insert(uri, values);
        }
        //refresh the toolbar menu
        getActivity().invalidateOptionsMenu();
    }

    public void delete() {
        //todo delete if no or empty wallet, else make inactive
    }


    private class HomePagerAdapter extends FragmentPagerAdapter {

        public HomePagerAdapter(FragmentManager fm, Currency currency) {
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
         * returns is what is displayed in the {@link io.ucoin.app.technical.view.SlidingTabLayout}.
         * <p>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0)
                return getString(R.string.rules);
            else
                return getString(R.string.peers);
        }

        @Override
        public android.app.Fragment getItem(int i) {

            android.app.Fragment fragment;

            // Rules page
            if(i == 0) {
                Currency currency = (Currency) getArguments()
                        .getSerializable(Currency.class.getSimpleName());
                fragment =  CurrencyRulesFragment.newInstance(currency);
            }

            // Network page
            else {
                Currency currency = (Currency) getArguments()
                        .getSerializable(Currency.class.getSimpleName());
                fragment = CurrencyNetworkFragment.newInstance(currency);
                fragment.setHasOptionsMenu(true);
            }

            return fragment;
        }
    }

}
