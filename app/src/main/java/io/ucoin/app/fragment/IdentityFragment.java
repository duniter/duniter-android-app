package io.ucoin.app.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
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

    private static String TAG = "IdentityFragment";
    private static String ARGS_TAB_INDEX = "tabIndex";

    private ProgressViewAdapter mProgressViewAdapter;
    private View mHeaderView;
    private CertificationListAdapter mCertificationListAdapter;
    private int headerHeight;
    private int minHeaderTranslation;
    private int toolbarTitleLeftMargin;
    private int toolbarTitleTextSize;
    private ImageButton mFavorite;
    private TextView mUidView;
    private TextView mTimestampView;
    private TabHost mTabs;
    private ListView mWotListView;
    private int mWotListScrollState;

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

        mHeaderView = (View) view.findViewById(R.id.header_layout);
        headerHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        toolbarTitleLeftMargin = getResources().getDimensionPixelSize(R.dimen.header_height);
        toolbarTitleTextSize = getResources().getDimensionPixelSize(R.dimen.header_text_size);
        minHeaderTranslation = -headerHeight +
                getResources().getDimensionPixelOffset(R.dimen.action_bar_height);

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
        mUidView = (TextView) view.findViewById(R.id.uid);
        mUidView.setText(identity.getUid());

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
        mWotListView = (ListView) view.findViewById(R.id.wot_list);
        mWotListView.setVisibility(View.GONE);
        mCertificationListAdapter = new CertificationListAdapter(getActivity());
        mWotListView.setAdapter(mCertificationListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        mWotListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get certification
                WotCertification cert = (WotCertification) mCertificationListAdapter
                        .getItem(position);

                onCertificationClick(cert);
            }
        });

        AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View c = view.getChildAt(0);

                if (c == null)
                    return;

                int firstVisiblePosition = view.getFirstVisiblePosition();
                int top = c.getTop();


                int scrollY = top;
                if (firstVisiblePosition >= 1) {
                    scrollY -= firstVisiblePosition * c.getHeight(); // + headerHeight;
                }

                // This will collapse the header when scrolling, until its height reaches
                // the toolbar height
                int translationY = scrollY; //Math.max(0, scrollY/* + minHeaderTranslation*/);
                if (translationY < minHeaderTranslation)
                    translationY = minHeaderTranslation;
                mHeaderView.setTranslationY(translationY);

                // Scroll ratio (0 <= ratio <= 1).
                // The ratio value is 0 when the header is completely expanded,
                // 1 when it is completely collapsed
                float offset = (float) (translationY) / minHeaderTranslation;


                // Now that we have this ratio, we only have to apply translations, scales,
                // alpha, etc. to the header views

                // For instance, this will move the toolbar title & subtitle on the X axis
                // from its original position when the ListView will be completely scrolled
                // down, to the Toolbar title position when it will be scrolled up.
                mUidView.setTranslationX(toolbarTitleLeftMargin * offset);
                //mUidView.setTranslationY(heard * offset);
                //mUidView.setTextSize(TypedValue.COMPLEX_UNIT_PX, toolbarTitleTextSize * offset);

                // Or we can make the FAB disappear when the ListView is scrolled
                mTimestampView.setAlpha(1- offset);
                mFavorite.setAlpha(1- offset);
                mTabs.setTranslationY(translationY);
                //mTabs.setTop(mHeaderView.getBottom());
            }

        };

        // TODO : make this works
        //mWotListView.setOnScrollListener(onScrollListener);

        //PROGRESS VIEW
        View progressView = view.findViewById(R.id.load_progress);
        progressView.setVisibility(View.VISIBLE);
        mProgressViewAdapter = new ProgressViewAdapter(
                progressView,
                mWotListView);

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        // Load WOT data
        LoadTask task = new LoadTask(identity);
        task.execute((Void) null);
    }

    // Method that allows us to get the scroll Y position of the ListView
    public int getScrollY(AbsListView view)
    {
        View c = view.getChildAt(0);

        if (c == null)
            return 0;

        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1)
            headerHeight = this.headerHeight;

        return top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_identity, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        activity.setTitle(R.string.identity);
        if (activity instanceof IToolbarActivity) {
            ((IToolbarActivity) activity).setToolbarBackButtonEnabled(true);
            ((IToolbarActivity) activity).setToolbarDrawable(getResources().getDrawable(R.drawable.shape_identity_toolbar));
        }
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

        /*Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        //intent.putExtra(ContactsContract.CommonDataKinds.Im.DISPLAY_NAME, "test");
        intent.putExtra(ContactsContract.CommonDataKinds.Im.DATA, "ucoin");
        intent.putExtra(ContactsContract.CommonDataKinds.Im.TYPE, ContactsContract.CommonDataKinds.Im.TYPE_WORK);
        intent.putExtra(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL);
        intent.putExtra(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, "ucoin");

        startActivity(intent);
        */

        DialogFragment fragment = AddContactDialogFragment.newInstance(identity);
        fragment.show(getFragmentManager(),
                fragment.getClass().getSimpleName());

        /*
        Fragment fragment = AddContactDialogFragment.newInstance(identity);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_in_down,
                        R.animator.slide_out_up,
                        R.animator.slide_in_up,
                        R.animator.slide_out_down)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
                */
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
            super(getActivity());
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
