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

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.CommunityCursorAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.content.Provider;
import io.ucoin.app.database.Contract;
import io.ucoin.app.model.BlockchainBlock;
import io.ucoin.app.model.BlockchainParameter;
import io.ucoin.app.model.Community;
import io.ucoin.app.model.Peer;
import io.ucoin.app.technical.AsyncTaskHandleException;


public class CommunityListFragment extends ListFragment
        implements AddNodeDialogFragment.OnClickListener{
    private ProgressViewAdapter mProgressViewAdapter;

    static public CommunityListFragment newInstance() {
        return new CommunityListFragment();
    }

    @Override
    public void onPositiveClick(Bundle args) {
        mProgressViewAdapter.showProgress(true);
        Peer peer = (Peer) args.getSerializable(Peer.class.getSimpleName());

        LoadCommunityTask task = new LoadCommunityTask();
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
        return inflater.inflate(R.layout.fragment_community_list,
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

        Uri uri = Uri.parse(Provider.CONTENT_URI + "/community/");
        String selection = Contract.Community.ACCOUNT_ID + "=?";
        String[] selectionArgs = {
                ((Application) getActivity().getApplication()).getAccountId()
        };

        Cursor cursor = getActivity().getContentResolver().query(uri, new String[]{}, selection,
                selectionArgs, null);

        CommunityCursorAdapter communityCursorAdapter =
                new CommunityCursorAdapter(getActivity(), cursor, 0);

        setListAdapter(communityCursorAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.toolbar_community_list, menu);
            getActivity().setTitle(R.string.communities);
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
        Community community = new Community(cursor);
        Fragment fragment = CommunityFragment.newInstance(community);
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

    public class LoadCommunityTask extends AsyncTaskHandleException<Peer, Void, Community> {

        private Activity mActivity = getActivity();

        @Override
        protected void onPreExecute() {
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Community doInBackgroundHandleException(Peer... peers) throws Exception {
             // Load currency
            URL url = new URL("http", peers[0].getUrl(), peers[0].getPort(),
                    "/blockchain/parameters");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            InputStream stream = conn.getInputStream();
            BlockchainParameter parameter = BlockchainParameter.fromJson(stream);

            //Load first block
            url = new URL("http", peers[0].getUrl(), peers[0].getPort(),
                    "/blockchain/block/0");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            stream = conn.getInputStream();
            BlockchainBlock firstBlock = BlockchainBlock.fromJson(stream);

            //Load last block
            url = new URL("http", peers[0].getUrl(), peers[0].getPort(),
                    "/blockchain/current");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            stream = conn.getInputStream();
            BlockchainBlock lastBlock = BlockchainBlock.fromJson(stream);

            return new Community(parameter, firstBlock, lastBlock, peers);
        }

        @Override
        protected void onSuccess(Community community) {
            mProgressViewAdapter.showProgress(false);
            Fragment fragment = CommunityFragment.newInstance(community);
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
