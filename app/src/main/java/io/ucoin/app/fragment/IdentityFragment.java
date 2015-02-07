package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.WotExpandableListAdapter;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.model.WotIdentityCertifications;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.DateUtils;


public class IdentityFragment extends Fragment {

    private ProgressViewAdapter mProgressViewAdapter;
    private WotExpandableListAdapter mWotListAdapter;
    private TextView mTimestampView;

    private boolean mSignatureSingleLine = true;
    private boolean mPubKeySingleLine = true;

    public static IdentityFragment newInstance(Identity identity) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
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
        super.onCreate(savedInstanceState);
        return inflater.inflate(R.layout.fragment_identity,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        // Tab host
        TabHost tabs = (TabHost)view.findViewById(R.id.tabHost);
        tabs.setup();
        {
            TabHost.TabSpec spec = tabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator(getString(R.string.identity_details));
            tabs.addTab(spec);
        }
        {
            TabHost.TabSpec spec = tabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator(getString(R.string.community));
            tabs.addTab(spec);
        }

        //Uid
        TextView uidView = (TextView) view.findViewById(R.id.uid);
        uidView.setText(identity.getUid());

        // Timestamp
        mTimestampView = (TextView) view.findViewById(R.id.timestamp);
        mTimestampView.setText(DateUtils.format(identity.getTimestamp()));

        // Signature
        final TextView signatureView = (TextView) view.findViewById(R.id.signature);
        signatureView.setText(identity.getSignature());

        // Pub key
        final TextView pubkeyView = (TextView) view.findViewById(R.id.pubkey);
        pubkeyView.setText(identity.getPubkey());

        // Wot list
        ExpandableListView wotListView = (ExpandableListView) view.findViewById(R.id.wot_list_view);
        wotListView.setVisibility(View.GONE);
        mWotListAdapter = new WotExpandableListAdapter(getActivity()) {
            @Override
            public String getGroupText(int groupPosition) {
                return groupPosition == 0 ? getString(R.string.certified_by) : getString(R.string.certifiers_of);
            }
        };
        mWotListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
        wotListView.setAdapter(mWotListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        wotListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // Get certification
                WotCertification cert = (WotCertification) mWotListAdapter
                        .getChild(groupPosition, childPosition);

                Fragment fragment = IdentityFragment.newInstance(cert);
                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.slide_in_right,
                                R.animator.slide_out_left)
                        .remove(IdentityFragment.this)
                        .commit();

                fragmentManager.popBackStack();

                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.slide_in_right,
                                R.animator.slide_out_left,
                                R.animator.delayed_fade_in,
                                R.animator.slide_out_up)
                        .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();

                return true;
            }
        });

        //PROGRESS VIEW
        View progressView = view.findViewById(R.id.load_progress);
        progressView.setVisibility(View.VISIBLE);
        mProgressViewAdapter = new ProgressViewAdapter(
                progressView,
                wotListView);

        // Load WOT data
        mProgressViewAdapter.showProgress(true);
        LoadTask task = new LoadTask(identity);
        task.execute((Void) null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_identity, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getActivity().setTitle(R.string.identity);
        ((MainActivity)getActivity()).setBackButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Bundle newInstanceArgs = getArguments();
        Identity identity = (Identity)
                newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        switch (item.getItemId()) {
            case R.id.action_transfer:
                Fragment fragment = TransferFragment.newInstance(identity);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.animator.slide_in_down,
                                R.animator.slide_out_up,
                                R.animator.slide_in_up,
                                R.animator.slide_out_down)
                        .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                        .addToBackStack(fragment.getClass().getSimpleName())
                        .commit();
                return true;
            case R.id.action_sign:
                doSign(identity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void doSign(Identity identity) {

        // Disable sign button
        //mSignButton.setActivated(false);

        // Execute sign task
        SignTask task = new SignTask(identity);
        task.execute((Void) null);
    }

    protected void onError(Throwable t) {
        Toast.makeText(getActivity(),
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();

    }

    public class LoadTask extends AsyncTaskHandleException<Void, Void, SparseArray<WotIdentityCertifications>> {
        private final Identity mIdentity;

        LoadTask(Identity identity) {
            mIdentity = identity;
        }

        @Override
        protected SparseArray<WotIdentityCertifications> doInBackgroundHandleException(Void... params) {
            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();

            // Reload identity (if need)
            if (mIdentity.getTimestamp() == -1) {
                Identity refreshIdentity = service.getIdentity(mIdentity.getUid(), mIdentity.getPubkey());
                mIdentity.setTimestamp(refreshIdentity.getTimestamp());
            }

            SparseArray<WotIdentityCertifications> results = new SparseArray<>();

            // Certified by
            WotIdentityCertifications certifiedBy = service.getCertifiedBy(mIdentity.getPubkey());
            if (certifiedBy == null
                    || certifiedBy.getCertifications() == null) {
                certifiedBy = new WotIdentityCertifications();
                certifiedBy.setCertifications(new ArrayList<WotCertification>());
            }
            results.append(0, certifiedBy);

            // Certifiers of
            WotIdentityCertifications certifiersOf = service.getCertifiersOf(mIdentity.getPubkey());
            if (certifiersOf == null
                    || certifiersOf.getCertifications() == null) {
                certifiersOf = new WotIdentityCertifications();
                certifiersOf.setCertifications(new ArrayList<WotCertification>());
            }
            results.append(1, certifiedBy);

            return results;
        }

        @Override
        protected void onSuccess(SparseArray<WotIdentityCertifications> wotCertifications) {

            // Update WOT
            if (wotCertifications == null || wotCertifications.size() == 0) {
                mWotListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
                return;
            }
            mWotListAdapter.setItems(wotCertifications);

            // update timestamp
            mTimestampView.setText(DateUtils.format(mIdentity.getTimestamp()));

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mWotListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
            mProgressViewAdapter.showProgress(false);
            onError(t);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

    /**
     * Sign a user
     */
    public class SignTask extends AsyncTaskHandleException<Void, Void, Boolean> {

        private final Identity mIdentity;

        SignTask(Identity identity) {
            mIdentity = identity;
        }

        @Override
        protected Boolean doInBackgroundHandleException(Void... params) {
            Configuration config = Configuration.instance();
            Wallet wallet = config.getCurrentWallet();

            WotRemoteService service = ServiceLocator.instance().getWotRemoteService();

            // Send certification
            String result = service.sendCertification(wallet, mIdentity);

            return true;
        }

        @Override
        protected void onSuccess(Boolean success) {
            if (success) {
                // TODO NLS
                Toast.makeText(getActivity(),
                        "Successfully sign " + mIdentity.getUid(),
                        Toast.LENGTH_SHORT).show();
            }
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mWotListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
            mProgressViewAdapter.showProgress(false);
            //mSignButton.setActivated(true);
            onError(t);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }
}
