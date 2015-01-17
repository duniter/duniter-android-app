package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import io.ucoin.app.R;
import io.ucoin.app.adapter.IdentityListAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Identity;


public class WotSearchFragment extends ListFragment{

    private OnIdentitySelectedListener onIdentitySelectedListener;

    private static final String TAG = "WotSearchFragment";

    private ListView mList;
    private IdentityListAdapter mIdentityListAdapter;
    private ProgressViewAdapter mProgressViewAdapter;

    static WotSearchFragment newInstance() {
        WotSearchFragment fragment = new WotSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onIdentitySelectedListener = (OnIdentitySelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnIdentitySelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIdentityListAdapter = new IdentityListAdapter() {
            @Override
            protected LayoutInflater getLayoutInflater() {
                return getActivity().getLayoutInflater();
            }
        };
        setListAdapter(mIdentityListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_search, container, false);
        }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mList = getListView();

        mProgressViewAdapter = new ProgressViewAdapter(
                view.findViewById(R.id.search_progress),
                mList);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Identity identity = (Identity)l.getAdapter().getItem(position);
        onIdentitySelectedListener.OnIdentitySelected(identity);
    }



    protected void onError(Throwable t) {
        Toast.makeText(getActivity(),
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();
        Log.e(TAG, t.getMessage());
    }

    public void callbackNewResult(List<Identity> identities)
    {
        mIdentityListAdapter.setItems(identities);
    }

    // Container Activity must implement this interface
    public interface OnIdentitySelectedListener {
        public void OnIdentitySelected(Identity identity);
    }

}
