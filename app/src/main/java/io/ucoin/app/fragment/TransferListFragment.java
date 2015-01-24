package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.CommunityCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.AsyncTaskHandleException;


public class TransferListFragment extends ListFragment {
    private CommunityCursorAdapter mCommunityCursorAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    static public TransferListFragment newInstance() {
        TransferListFragment fragment = new TransferListFragment();
        return fragment;
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
        return inflater.inflate(R.layout.fragment_transfer_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.progressbar),
                getListView());


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/community/");

        String selection = Contract.Community.ACCOUNT_ID + "=?";
        String[] selectionArgs = {sharedPref.getString("_id", "")};

        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, null,
                null, null);
        mCommunityCursorAdapter = new CommunityCursorAdapter((Context) getActivity(), cursor, 0);
        setListAdapter(mCommunityCursorAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_transfer_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.transactions);
        ((MainActivity) getActivity()).setBackButtonEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_transfer:
                Fragment fragment = TransferFragment.newInstance(null);
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
