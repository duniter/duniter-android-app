package io.ucoin.app.fragment;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.Currency;
import io.ucoin.app.model.Peer;

public class CurrencyFragment extends Fragment {


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
        getActivity().setTitle(getString(R.string.currency));
        ((MainActivity) getActivity()).setBackButtonEnabled(false);

        Bundle newInstanceArgs = getArguments();
        Currency currency = (Currency) newInstanceArgs
                .getSerializable(Currency.class.getSimpleName());

        TextView currencyName = (TextView) view.findViewById(R.id.currency);
        currencyName.setText(currency.getCurrencyName());

        TextView memberCount = (TextView) view.findViewById(R.id.members_count);
        memberCount.setText(getString(R.string.members_count, currency.getMembersCount()));
/*
        TextView mUd0 = (TextView) view.findViewById(R.id.ud0);
        TextView mSigDelay = (TextView) view.findViewById(R.id.sig_delay);
        TextView mSigValidity = (TextView) view.findViewById(R.id.sig_validity);
        TextView mSigQty = (TextView) view.findViewById(R.id.sig_qty);
        TextView mSigWoT = (TextView) view.findViewById(R.id.sig_woT);
        TextView mMsValidity = (TextView) view.findViewById(R.id.ms_validity);
        TextView mStepMax = (TextView) view.findViewById(R.id.step_max);
        TextView mMedianTimeBlocks = (TextView) view.findViewById(R.id.median_time_blocks);
        TextView mAvgGenTime = (TextView) view.findViewById(R.id.avg_gen_time);
        TextView mDtDiffEval = (TextView) view.findViewById(R.id.dt_diff_eval);
        TextView mBlocksRot = (TextView) view.findViewById(R.id.blocks_rot);
        TextView mPercentRot = (TextView) view.findViewById(R.id.percent_rot);

        TextView growth = (TextView) view.findViewById(R.id.growth);
        growth.setText(getString(R.string.growth) + ": " +
        Double.toString(parameter.getC()) + " / " + Integer.toString(parameter.getDt()));


        mUd0.setText(Integer.toString(parameter.getUd0()));
        mSigDelay.setText(Integer.toString(parameter.getSigDelay()));
        mSigValidity.setText(Integer.toString(parameter.getSigValidity()));
        mSigQty.setText(Integer.toString(parameter.getSigQty()));
        mSigWoT.setText(Integer.toString(parameter.getSigWoT()));
        mMsValidity.setText(Integer.toString(parameter.getMsValidity()));
        mStepMax.setText(Integer.toString(parameter.getStepMax()));
        mMedianTimeBlocks.setText(Integer.toString(parameter.getMedianTimeBlocks()));
        mAvgGenTime.setText(Integer.toString(parameter.getAvgGenTime()));
        mDtDiffEval.setText(Integer.toString(parameter.getDtDiffEval()));
        mBlocksRot.setText(Integer.toString(parameter.getBlocksRot()));
        mPercentRot.setText(Double.toString(parameter.getPercentRot()));
*/
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
        getActivity().setTitle(R.string.currency);
        ((MainActivity) getActivity()).setBackButtonEnabled(true);
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
}
