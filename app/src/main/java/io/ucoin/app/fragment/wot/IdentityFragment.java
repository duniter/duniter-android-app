package io.ucoin.app.fragment.wot;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import io.ucoin.app.R;
import io.ucoin.app.activity.IToolbarActivity;
import io.ucoin.app.adapter.CertificationListAdapter;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.fragment.contact.AddContactDialogFragment;
import io.ucoin.app.fragment.wallet.TransferFragment;
import io.ucoin.app.model.remote.Identity;
import io.ucoin.app.model.remote.WotCertification;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.CollectionUtils;
import io.ucoin.app.technical.DateUtils;
import io.ucoin.app.technical.ObjectUtils;
import io.ucoin.app.technical.StringUtils;
import io.ucoin.app.technical.ViewUtils;
import io.ucoin.app.technical.task.AsyncTaskHandleException;
import io.ucoin.app.technical.view.AlphaForegroundColorSpan;
import io.ucoin.app.technical.view.NotifyingScrollView;


public class IdentityFragment extends Fragment {

    private static String TAG = "IdentityFragment";
    private static String HEADER_INDEX = "tabIndex";

    private ProgressViewAdapter mProgressViewAdapter;
    private View mHeader;
    private View mContent;
    private CertificationListAdapter mCertificationListAdapter;
    private int mActionBarHeight;
    private int mHeaderHeight;

    private int mHeaderHeight2;
    private int mMinHeaderTranslation;
    private TextView mUidView;
    private ImageButton mIcon;
    private View mDetailLayout;
    private TextView mTimestampView;
    private TextView mSignatureView;
    private ListView mListView;
    private TextView mPubkeyView;

    private RectF mRect1 = new RectF();
    private RectF mRect2 = new RectF();

    private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
    private SpannableString mActionBarTitleSpannableString;

    private AccelerateDecelerateInterpolator mSmoothInterpolator;
    private TypedValue mTypedValue = new TypedValue();

    public static IdentityFragment newInstance(Identity identity) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
        newInstanceArgs.putInt(HEADER_INDEX, 0);
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    public static IdentityFragment newInstance(Identity identity, int headerIndex) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putSerializable(Identity.class.getSimpleName(), identity);
        newInstanceArgs.putInt(HEADER_INDEX, headerIndex);
        fragment.setArguments(newInstanceArgs);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private View mPlaceHolderView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_identity,
                container, false);

        // List view (WoT)
        mListView = (ListView) view.findViewById(R.id.wot_list);

        // Set the placeholder header
        //mPlaceHolderView = inflater.inflate(R.layout.fake_header, mListView, false);
        //mListView.addHeaderView(mPlaceHolderView);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle newInstanceArgs = getArguments();
        final Identity identity = (Identity) newInstanceArgs
                .getSerializable(Identity.class.getSimpleName());

        // Header
        mHeader = (View) view.findViewById(R.id.header_layout);

        mContent = (View) view.findViewById(R.id.content_layout);

        mSmoothInterpolator = new AccelerateDecelerateInterpolator();
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mHeaderHeight2 = getResources().getDimensionPixelSize(R.dimen.header_height2);
        mMinHeaderTranslation = -mHeaderHeight + getActionBarHeight();

        // Action bar title
        mActionBarTitleSpannableString = new SpannableString(identity.getUid());
        mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(getResources().getColor(R.color.textPrimary));

        // Uid
        mUidView = (TextView) view.findViewById(R.id.uid);


        // Timestamp
        mTimestampView = (TextView) view.findViewById(R.id.timestamp);

        // Pub key
        mPubkeyView = (TextView) view.findViewById(R.id.pubkey);

        // Signature
        final View signatureIconView = view.findViewById(R.id.signature_icon);
        mSignatureView = (TextView) view.findViewById(R.id.signature);


        // Toogle detail button
        final ImageButton showMoreButton = (ImageButton) view.findViewById(R.id.more_button);
        showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreButton.setVisibility(View.GONE);
                signatureIconView.setVisibility(View.VISIBLE);
                mSignatureView.setVisibility(View.VISIBLE);
            }
        });

        // Wot list
        mListView = (ListView) view.findViewById(R.id.wot_list);
        mListView.setVisibility(View.GONE);
        mCertificationListAdapter = new CertificationListAdapter(getActivity());
        mListView.setAdapter(mCertificationListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position <= 0) {
                    return;
                }

                // Get certification, and open it
                WotCertification cert = (WotCertification) mCertificationListAdapter
                        .getItem(position-1);
                onCertificationClick(cert);
            }
        });

        //PROGRESS VIEW
        View progressView = view.findViewById(R.id.load_progress);
        progressView.setVisibility(View.VISIBLE);
        mProgressViewAdapter = new ProgressViewAdapter(
                progressView,
                mListView);

        // Make sure to hide the keyboard
        ViewUtils.hideKeyboard(getActivity());

        // Bind model to UI
        bindView(identity);

        setupListView(view);
    }

    private void setupListView(View view) {

        ((NotifyingScrollView) view.findViewById(R.id.scroll_view)).setOnScrollChangedListener(new NotifyingScrollView.OnScrollChangedListener() {
            public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                final int headerHeight = mHeader.getHeight() - getActionBarHeight();
                final float ratio = (float) Math.min(Math.max(t, 0), headerHeight) / headerHeight;
                final int newAlpha = (int) (ratio * 255);
                setTitleAlpha(newAlpha);
            }

/*
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int scrollY = getScrollY();
                //sticky actionbar
                mHeader.setTranslationY(Math.max(-scrollY, mMinHeaderTranslation));
                //header_logo --> actionbar icon
                float ratio = clamp(mHeader.getTranslationY() / mMinHeaderTranslation, 0.0f, 1.0f);
                //interpolate(mHeaderLogo, getActionBarIconView(), mSmoothInterpolator.getInterpolation(ratio));
                //actionbar title alpha
                if (titleView != null) {
                    titleView.setAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
                }
                //---------------------------------
                //better way thanks to @cyrilmottier
                setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
            }*/
        });
    }
    protected void bindView(Identity identity) {

        mUidView.setText(identity.getUid());
        mTimestampView.setText(getString(R.string.registered_since, DateUtils.formatLong(identity.getTimestamp())));

        // Pubkey
        String pubkey = identity.getPubkey();
        int offset = pubkey.length()/2;
        pubkey = pubkey.substring(0, offset) + '\n' + pubkey.substring(offset);
        mPubkeyView.setText(pubkey);

        // Signature
        String signature = identity.getSignature();
        if (StringUtils.isNotBlank(signature)) {
            offset = signature.length() / 3;
            signature = signature.substring(0, offset)
                    + '\n' + signature.substring(offset, offset * 2)
                    + '\n' + signature.substring(offset * 2);
        }
        else {
            signature = "";
        }
        mSignatureView.setText(signature);

        // Title
        getActivity().setTitle("");

        // Load WOT data
        LoadTask task = new LoadTask(identity);
        task.execute((Void) null);
    }

    // Method that allows us to get the scroll Y position of the ListView
    public int getScrollY() {
        View c = mListView.getChildAt(0);
        if (c == null) {
            return 0;
        }

        int firstVisiblePosition = mListView.getFirstVisiblePosition();
        int top = c.getTop();

        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mHeader.getHeight();
        }

        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    private void setTitleAlpha(float alpha) {
        mAlphaForegroundColorSpan.setAlpha(alpha);
        mActionBarTitleSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mActionBarTitleSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActivity().setTitle(mActionBarTitleSpannableString);
    }


    private void interpolate(View view1, View view2, float interpolation) {
        getOnScreenRect(mRect1, view1);
        getOnScreenRect(mRect2, view2);
        float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
        float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
        float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
        float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));
        view1.setTranslationX(translationX);
        view1.setTranslationY(translationY - mHeader.getTranslationY());
        view1.setScaleX(scaleX);
        view1.setScaleY(scaleY);
    }

    private RectF getOnScreenRect(RectF rect, View view) {
        rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        return rect;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_identity, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Activity activity = getActivity();
        //activity.setTitle(R.string.identity);
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
            case R.id.action_add_existing_contact:
                onAddAsExistingContact();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCertificationClick(WotCertification cert) {
        // Reset the cert time, before to reuse the identity
        cert.setTimestamp(-1);

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

    public int getActionBarHeight() {
        if (mActionBarHeight != 0) {
            return mActionBarHeight;
        }
        getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
        return mActionBarHeight;
    }

    protected void onAddAsExistingContact() {
        Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(i, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Object contact = data.getData();
                Toast.makeText(getActivity(), contact.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onError(Throwable t) {
        Toast.makeText(getActivity(),
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();

    }

    public class LoadTask extends AsyncTaskHandleException<Void, Void, Collection<WotCertification>> {
        private final Identity mIdentity;

        LoadTask(Identity identity) {
            super(getActivity().getApplicationContext());
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
            mTimestampView.setText(getString(R.string.registered_since, DateUtils.formatLong(mIdentity.getTimestamp())));

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
