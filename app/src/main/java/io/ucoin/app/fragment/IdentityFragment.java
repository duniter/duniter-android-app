package io.ucoin.app.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;

import io.ucoin.app.R;
import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.adapter.WotExpandableListAdapter;
import io.ucoin.app.config.Configuration;
import io.ucoin.app.model.Identity;
import io.ucoin.app.model.Wallet;
import io.ucoin.app.model.WotCertification;
import io.ucoin.app.model.WotIdentityCertifications;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.service.WotService;
import io.ucoin.app.technical.AsyncTaskHandleException;
import io.ucoin.app.technical.DateUtils;


public class IdentityFragment extends Fragment {

    public static final String PARAM_IDENTITY = "identity";
    private static final String TAG = "IdentityActivity";

    private ProgressViewAdapter mProgressViewAdapter;
    private WotExpandableListAdapter mWotListAdapter;

    private EditText mTimestampView;
    private EditText mSignatureView;
    private EditText mPubkeyView;
    private Button mSignButton;
    private Button mTransferButton;
    private ExpandableListView mWotListView;
    private Identity mIdentity;
    private boolean mSignatureSingleLine = true;
    private boolean mPubKeySingleLine = true;

    public static IdentityFragment newInstance(Identity identity) {
        IdentityFragment fragment = new IdentityFragment();
        Bundle args = new Bundle();
        args.putSerializable(null, identity);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mIdentity = (Identity)getArguments().getSerializable(null);
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

        mTimestampView = (EditText) view.findViewById(R.id.timestamp);

        // Signature
        mSignatureView = (EditText)view.findViewById(R.id.signature);
        mSignatureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignatureSingleLine = !mSignatureSingleLine;
                mSignatureView.setSingleLine(!mSignatureSingleLine);
            }
        });

        // Pub key
        mPubkeyView = (EditText)view.findViewById(R.id.pubkey);
        mPubkeyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPubKeySingleLine = !mPubKeySingleLine;
                mPubkeyView.setSingleLine(mPubKeySingleLine);
            }
        });

        mSignButton = (Button) view.findViewById(R.id.sign_button);
        mSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSign();
            }
        });

        mTransferButton = (Button) view.findViewById(R.id.transfer_button);
        mTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTransfer();
            }
        });

        // Wot list
        mWotListView = (ExpandableListView) view.findViewById(R.id.wot_list_view);
        mWotListView.setVisibility(View.GONE);
        mWotListAdapter = new WotExpandableListAdapter(getActivity()){
            @Override
            public String getGroupText(int groupPosition) {
                return groupPosition == 0 ? getString(R.string.certified_by_label) : getString(R.string.certifiers_of_label);
            }
        };
        mWotListView.setAdapter(mWotListAdapter);

        //this listener is not called unless WotExpandableListAdapter.isChildSelectable return true
        //and convertView.onClickListener is not set (in WotExpandableListAdapter)
        mWotListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // Get certification
                WotCertification cert = (WotCertification) mWotListAdapter.getChild(groupPosition, childPosition);
                updateIdentityView(cert);
                return true;
            }
        });

        View progressView = (View) view.findViewById(R.id.load_progress);
        progressView.setVisibility(View.VISIBLE);
        mProgressViewAdapter = new ProgressViewAdapter(
                progressView,
                mWotListView);


        updateIdentityView(mIdentity);
    }

    private void updateIdentityView(Identity identity)
    {
        mIdentity = identity;
        getActivity().setTitle(identity.getUid());
        mWotListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
        // Timestamp
        mTimestampView.setText(DateUtils.format(identity.getTimestamp()));
        // Signature
        mSignatureView.setText(identity.getSignature());
        // Pub key
        mPubkeyView.setText(identity.getPubkey());

        // Load WOT data
        mProgressViewAdapter.showProgress(true);
        LoadTask task = new LoadTask(mIdentity);
        task.execute((Void) null);
    }

    protected void doSign() {

        // Disable sign button
        mSignButton.setActivated(false);

        // Execute sign task
        SignTask task = new SignTask(mIdentity);
        task.execute((Void)null);
    }

    protected void doTransfer() {
        Fragment fragment =  TransferFragment.newInstance(mIdentity);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_content, fragment)
                .addToBackStack("TRANSFER_BACKSTACK")
                .commit();
    }

    protected void onError(Throwable t) {
        Toast.makeText(getActivity(),
                "Error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();

    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class LoadTask extends AsyncTaskHandleException<Void, Void, SparseArray<WotIdentityCertifications>> {

        private final Identity mIdentity;

        LoadTask(Identity identity) {
            mIdentity = identity;
        }

        @Override
        protected SparseArray<WotIdentityCertifications> doInBackgroundHandleException(Void... params) {

            SparseArray<WotIdentityCertifications> results = new SparseArray<WotIdentityCertifications>();
            WotService service = ServiceLocator.instance().getWotService();

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

            if (wotCertifications == null || wotCertifications.size() == 0) {
                mWotListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
                return;
            }

            mWotListAdapter.setItems(wotCertifications);
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

            WotService service = ServiceLocator.instance().getWotService();

            // Send certification
            String result = service.sendCertification(wallet, mIdentity);
            Log.d(TAG, result);

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
            mSignButton.setActivated(true);
            onError(t);
        }

        @Override
        protected void onCancelled() {
            mProgressViewAdapter.showProgress(false);
        }
    }
}
