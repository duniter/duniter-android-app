package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import io.ucoin.app.R;
import io.ucoin.app.activity.MainActivity;
import io.ucoin.app.adapter.CertificationListAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;


public class IdentityFragment extends Fragment {

    private static String ARGS_TAB_INDEX = "tabIndex";

    private ProgressViewAdapter mProgressViewAdapter;
    private CertificationListAdapter mCertificationListAdapter;
    private ImageButton mFavorite;
    private TextView mTimestampView;
    private TabHost mTabs;

    private boolean mSignatureSingleLine = true;
    private boolean mPubKeySingleLine = true;

    public static IdentityFragment newInstance(Identity identity) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
        newInstanceArgs.putInt(ARGS_TAB_INDEX, 0);
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    public static IdentityFragment newInstance(Identity identity, int tabIndex) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
        newInstanceArgs.putInt(ARGS_TAB_INDEX, tabIndex);
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
        final int tabIndex = newInstanceArgs.getInt(ARGS_TAB_INDEX);

        // Tab host
        mTabs = (TabHost)view.findViewById(R.id.tabHost);
        mTabs.setup();
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab1");
            spec.setContent(R.id.tab1);
            spec.setIndicator(getString(R.string.identity_details));
            mTabs.addTab(spec);
        }
        {
            TabHost.TabSpec spec = mTabs.newTabSpec("tab2");
            spec.setContent(R.id.tab2);
            spec.setIndicator(getString(R.string.community));
            mTabs.addTab(spec);
        }
        mTabs.setCurrentTab(tabIndex);

        //Uid
        TextView uidView = (TextView) view.findViewById(R.id.uid);
        uidView.setText(identity.getUid());

        // Icon
        mFavorite = (ImageButton)view.findViewById(R.id.favorite_button);
        // TODO: implement favorite button
        mFavorite.setVisibility(View.GONE);

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
        ListView wotListView = (ListView) view.findViewById(R.id.wot_list);
        wotListView.setVisibility(View.GONE);
        mCertificationListAdapter = new CertificationListAdapter(getActivity());
        wotListView.setAdapter(mCertificationListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        wotListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get certification
                WotCertification cert = (WotCertification) mCertificationListAdapter
                        .getItem(position);

                onCertificationClick(cert);
            }
        });

        //PROGRESS VIEW
        View progressView = view.findViewById(R.id.load_progress);
        progressView.setVisibility(View.VISIBLE);
        mProgressViewAdapter = new ProgressViewAdapter(
                progressView,
                wotListView);

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        // Load WOT data
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
        switch (item.getItemId()) {
            case R.id.action_transfer:
                onTransferClick();
                return true;
            case R.id.action_sign:
                onSignClick();
                return true;
            case R.id.action_add_contact:
                onAddAsNewContact();
                return true;
            /*case R.id.action_add_existing_contact:
                onAddAsExistingContact();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCertificationClick(WotCertification cert) {
        // Reset the cert time, before to resue the identity
        cert.setTimestamp(-1);

        Fragment fragment = IdentityFragment.newInstance(cert, mTabs.getCurrentTab());

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
    }

    protected void onTransferClick() {
        Bundle newInstanceArgs = getArguments();
        Identity identity = (Identity)
                newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        Fragment fragment = TransferFragment.newInstance(identity);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onSignClick() {
        Bundle newInstanceArgs = getArguments();
        Identity identity = (Identity)
                newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        Fragment fragment = SignFragment.newInstance(identity);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onAddAsNewContact() {
        Bundle newInstanceArgs = getArguments();
        Identity identity = (Identity)
                newInstanceArgs.getSerializable(Identity.class.getSimpleName());

        Fragment fragment = AddContactFragment.newInstance(identity);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    protected void onAddAsExistingContact() {
        Toast.makeText(getActivity(),
                "Not implemented yet. But cooming soon !",
                Toast.LENGTH_LONG).show();
    }


    protected void onError(Throwable t) {
        Toast.makeText(getActivity(),
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();

    }

    public class LoadTask extends AsyncTaskHandleException<Void, Void, Collection<WotCertification>> {
        private final Identity mIdentity;

        LoadTask(Identity identity) {
            ObjectUtils.checkNotNull(identity);
            ObjectUtils.checkNotNull(identity.getCurrencyId());
            mIdentity = identity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressViewAdapter.showProgress(true);
        }

        @Override
        protected Collection<WotCertification> doInBackgroundHandleException(Void... params) {
            // Refresh the membership data
            BlockchainRemoteService bcService = ServiceLocator.instance().getBlockchainRemoteService();
            bcService.loadMembership(mIdentity.getCurrencyId(), mIdentity, false);

            // Get certifications
            WotRemoteService wotService = ServiceLocator.instance().getWotRemoteService();
            return wotService.getCertifications(
                    mIdentity.getCurrencyId(),
                    mIdentity.getUid(),
                    mIdentity.getPubkey(),
                    mIdentity.isMember());
        }

        @Override
        protected void onSuccess(Collection<WotCertification> certifications) {

            // Refresh timestamp
            mTimestampView.setText(DateUtils.format(mIdentity.getTimestamp()));

            // Refresh star
            //mFavorite.setImageResource(ImageAdapterHelper.getImageWhite(mIdentity));

            // Update certification list
            mCertificationListAdapter.clear();
            if (CollectionUtils.isNotEmpty(certifications)) {
                mCertificationListAdapter.addAll(certifications);
                mCertificationListAdapter.notifyDataSetChanged();
            }

            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mCertificationListAdapter.clear();
            mProgressViewAdapter.showProgress(false);
            onError(t);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }

}
