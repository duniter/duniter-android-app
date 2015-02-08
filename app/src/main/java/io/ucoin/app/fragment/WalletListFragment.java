package io.ucoin.app.fragment;

import android.app.ListFragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.adapter.CurrencyCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;


public class WalletListFragment extends ListFragment{
    private ProgressViewAdapter mProgressViewAdapter;

    static public WalletListFragment newInstance() {
        return new WalletListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_wallet_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                getListView());

        TextView v = (TextView) view.findViewById(android.R.id.empty);
        v.setVisibility(View.GONE);

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/wallet/");
        String selection = Contract.Wallet.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                ((Application) getActivity().getApplication()).getAccountId()
        };

        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, null);

        CurrencyCursorAdapter currencyCursorAdapter =
                new CurrencyCursorAdapter(getActivity(), cursor, 0);

        setListAdapter(currencyCursorAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            /*inflater.inflate(R.menu.toolbar_currency_list, menu);
            ((MainActivity) getActivity()).setBackButtonEnabled(false);
            ((MainActivity) getActivity()).
                    setToolbarColor(getResources().getColor(R.color.primary));*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                // TODO : show a "new wallet fragment"
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        // TODO : open transaction with a filter on this wallet ?
        /*Wallet wallet = ServiceLocator.instance().getWalletService().read(cursor);
        Fragment fragment = WalletFragment.newInstance(wallet);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_slide_in_up,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.slide_out_up)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();*/
    }

}
