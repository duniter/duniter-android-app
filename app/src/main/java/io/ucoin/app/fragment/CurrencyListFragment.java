package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.widget.Toast;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.CurrencyCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Peer;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;


public class CurrencyListFragment extends ListFragment
        implements AddNodeDialogFragment.OnClickListener{
    private ProgressViewAdapter mProgressViewAdapter;

    static public CurrencyListFragment newInstance() {
        return new CurrencyListFragment();
    }

    @Override
    public void onPositiveClick(Bundle args) {
        mProgressViewAdapter.showProgress(true);
        Peer peer = (Peer) args.getSerializable(Peer.class.getSimpleName());

        LoadCurrencyTask task = new LoadCurrencyTask();
        task.execute(peer);
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
        return inflater.inflate(R.layout.fragment_currency_list,
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

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/currency/");
        // TODO kimamila : filter ?
        String selection = Contract.Currency.ACCOUNT_ID + "=?";
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
            inflater.inflate(R.menu.toolbar_currency_list, menu);
            getActivity().setTitle(R.string.currencies);
            ((MainActivity) getActivity()).setBackButtonEnabled(false);
            ((MainActivity) getActivity()).
                    setToolbarColor(getResources().getColor(R.color.primary));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                AddNodeDialogFragment fragment = AddNodeDialogFragment.newInstance(this);
                fragment.show(getFragmentManager(),
                        fragment.getClass().getSimpleName());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        Currency currency = ServiceLocator.instance().getDataService().toCurrency(cursor);
        Fragment fragment = CurrencyFragment.newInstance(currency);
        FragmentManager fragmentManager = getFragmentManager();
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

    public class LoadCurrencyTask extends AsyncTaskHandleException<Peer, Void, Currency> {

        private Activity mActivity = getActivity();

        @Override
        protected void onPreExecute() {
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Currency doInBackgroundHandleException(Peer... peers) throws Exception {
            Currency currency = ServiceLocator.instance().getBlockchainRemoteService()
                    .getCurrencyFromPeer(peers[0]);

            return currency;
        }

        @Override
        protected void onSuccess(Currency currency) {
            mProgressViewAdapter.showProgress(false);
            Fragment fragment = CurrencyFragment.newInstance(currency);
            FragmentManager fragmentManager = getFragmentManager();
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

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);
            Toast.makeText(mActivity,
                    t.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}
