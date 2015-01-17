package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.IdentityArrayAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Identity;


public class WotSearchFragment extends ListFragment{

    private static final String TAG = "WotSearchFragment";

    private IdentityArrayAdapter mIdentityArrayAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    static WotSearchFragment newInstance() {
        WotSearchFragment fragment = new WotSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIdentityArrayAdapter = new IdentityArrayAdapter(getActivity());
        setListAdapter(mIdentityArrayAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_search, container, false);
        }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.search_progress),
                getListView());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Identity identity = (Identity)l.getAdapter().getItem(position);
        Fragment fragment =  IdentityFragment.newInstance(identity);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_content, fragment)
                .addToBackStack("SEARCH_BACKSTACK")
                .commit();

        ((MainActivity) getActivity()).collapseSearchView();
    }

    public void callbackNewResult(List<Identity> identities)
    {
        mIdentityArrayAdapter.clear();
        mIdentityArrayAdapter.addAll(identities);
        mIdentityArrayAdapter.notifyDataSetChanged();
    }
}
