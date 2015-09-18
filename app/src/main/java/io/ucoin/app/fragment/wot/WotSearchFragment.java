package io.ucoin.app.fragment.wot;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.app.SearchManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.IdentityArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.remote.Identity;


public class WotSearchFragment extends ListFragment
        implements MainActivity.QueryResultListener<Identity>{


    private IdentityArrayAdapter mIdentityAdapter;
    private ProgressViewAdapter mProgressViewAdapter;
    private SearchView mSearchView;
    private boolean isWaitingResult = true;

    static public WotSearchFragment newInstance(String query) {
        WotSearchFragment fragment = new WotSearchFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putString("query", query);
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    static public void setArguments(WotSearchFragment fragment, String query) {
        Bundle newInstanceArgs = fragment.getArguments();
        newInstanceArgs.putString("query", query);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mIdentityAdapter = new IdentityArrayAdapter(getActivity());
        setListAdapter(mIdentityAdapter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_search,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Only display progress is waiting query result
        // (could false when user return here using the back menu)
        if (isWaitingResult) {
            mProgressViewAdapter = new ProgressViewAdapter(
                    view.findViewById(R.id.search_progress),
                    getListView());
            // Display the progress by default (onQuerySuccess will disable it)
            mProgressViewAdapter.showProgress(isWaitingResult);

            TextView v = (TextView) view.findViewById(android.R.id.empty);
            v.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_search, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Bundle newInstanceArgs = getArguments();
        String query = newInstanceArgs
                .getString("query");

        Activity activity = getActivity();
        activity.setTitle("/" + query);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
        }

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Activity.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        mSearchView = (SearchView)searchItem.getActionView();
        mSearchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        mSearchView.setIconified(false);
        mSearchView.setQuery(query, false);

        //hide the keyboard and remove focus
        mSearchView.clearFocus();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // DO not pop, because this will reload the home fragment
                //getFragmentManager().popBackStack();

                mProgressViewAdapter.showProgress(true);
                isWaitingResult = true;
                return ((MainActivity) getActivity()).onQueryTextSubmit(searchItem, query);
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
         return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Identity identity = (Identity) l.getAdapter().getItem(position);
        Fragment fragment = IdentityFragment.newInstance(identity);
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
    public void onQuerySuccess(List<? extends Identity> identities) {
        mIdentityAdapter.setNotifyOnChange(false);
        mIdentityAdapter.clear();
        mIdentityAdapter.addAll(identities);
        mIdentityAdapter.notifyDataSetChanged();
        mProgressViewAdapter.showProgress(false);
        isWaitingResult = false;
    }

    @Override
    public void onQueryFailed(String message) {
        mProgressViewAdapter.showProgress(false);
        isWaitingResult = false;

        Toast.makeText(getActivity(),
                getString(R.string.search_error, message),
                Toast.LENGTH_LONG);
    }

    @Override
    public void onQueryCancelled() {
        mProgressViewAdapter.showProgress(false);
        isWaitingResult = false;
    }
}
