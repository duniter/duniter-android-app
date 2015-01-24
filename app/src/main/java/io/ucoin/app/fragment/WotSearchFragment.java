package io.ucoin.app.fragment;

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

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.IdentityArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Identity;


public class WotSearchFragment extends ListFragment
        implements MainActivity.QueryResultListener{


    private IdentityArrayAdapter mIdentityArrayAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    static public WotSearchFragment newInstance(String query) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putString("query", query);

        WotSearchFragment fragment = new WotSearchFragment();
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mIdentityArrayAdapter = new IdentityArrayAdapter(getActivity());
        setListAdapter(mIdentityArrayAdapter);
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

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.search_progress),
                getListView());

        TextView v = (TextView) view.findViewById(android.R.id.empty);
        v.setVisibility(View.GONE);
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

        getActivity().setTitle("/" + query);
        ((MainActivity)getActivity()).setBackButtonEnabled(true);

        SearchManager searchManager = (SearchManager) getActivity()
                .getSystemService(Activity.SEARCH_SERVICE);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconified(false);
        searchView.setQuery(query, false);

        //hide the keyboard and remove focus
        searchView.clearFocus();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getFragmentManager().popBackStack();
                return ((MainActivity) getActivity()).onQueryTextSubmit(searchItem, s);
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
    public void onQuerySuccess(List<Identity> identities) {
        mProgressViewAdapter.showProgress(false);
        mIdentityArrayAdapter.clear();
        mIdentityArrayAdapter.addAll(identities);
        mIdentityArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onQueryFailed() {
        mProgressViewAdapter.showProgress(false);
    }

    @Override
    public void onQueryCancelled() {
        mProgressViewAdapter.showProgress(false);
    }
}
