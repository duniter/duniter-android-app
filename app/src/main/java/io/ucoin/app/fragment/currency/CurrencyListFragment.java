package io.ucoin.app.fragment.currency;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
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
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.adapter.CurrencyCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.database.Contract;
import io.ucoin.app.database.Provider;
import io.ucoin.app.model.local.Peer;
import io.ucoin.app.model.remote.Currency;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.local.CurrencyService;
import io.ucoin.app.technical.ExceptionUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.UCoinTechnicalException;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class CurrencyListFragment extends ListFragment {
    private ProgressViewAdapter mProgressViewAdapter;

    static public CurrencyListFragment newInstance() {
        return new CurrencyListFragment();
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
        String selection = Contract.Currency.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                ((Application) getActivity().getApplication()).getAccountIdAsString()
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
        Activity activity = getActivity();
        activity.setTitle(R.string.currencies);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(false);
            ((IToolbarActivity) activity).setToolbarColor(getResources().getColor(R.color.primary));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_currency:
                onAddCurrencyClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        Currency currency = ServiceLocator.instance().getCurrencyService().toCurrency(cursor);
        onOpenCurrency(currency);
    }


    private void onAddCurrencyClick() {
        DialogFragment fragment = AddCurrencyDialogFragment.newInstance(new AddCurrencyDialogFragment.OnClickListener() {
            @Override
            public void onPositiveClick(Bundle args) {
                Peer peer = (Peer) args.getSerializable(Peer.class.getSimpleName());
                AddCurrencyTask task = new AddCurrencyTask();
                task.execute(peer);
            }
        });
        fragment.show(getFragmentManager(),
                fragment.getClass().getSimpleName());
    }

    private void onOpenCurrency(final Currency currency) {
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

    public class AddCurrencyTask extends AsyncTaskHandleException<Peer, Void, Currency> {

        public AddCurrencyTask() {
            super(getActivity());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Currency doInBackgroundHandleException(Peer... peers) throws Exception {
            // Load currency from node
            Currency currency = ServiceLocator.instance().getBlockchainRemoteService()
                    .getCurrencyFromPeer(peers[0]);

            // save it
            if (currency != null && StringUtils.isNotBlank(currency.getCurrencyName())) {
                CurrencyService currencyService = ServiceLocator.instance().getCurrencyService();

                Long existingCurrencyId = currencyService.getCurrencyIdByName(currency.getCurrencyName());
                if (existingCurrencyId != null) {
                    throw new UCoinTechnicalException(getString(R.string.duplicate_currency_name, currency.getCurrencyName()));
                }

                // Save currency into DB
                currency = currencyService.save(getContext(), currency);
            }

            return currency;
        }

        @Override
        protected void onSuccess(Currency currency) {
            mProgressViewAdapter.showProgress(false);
            if (currency != null) {
                onOpenCurrency(currency);
            }
        }

        @Override
        protected void onFailed(Throwable t) {
            mProgressViewAdapter.showProgress(false);
            Toast.makeText(getContext(),
                    ExceptionUtils.getMessage(t),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
}
