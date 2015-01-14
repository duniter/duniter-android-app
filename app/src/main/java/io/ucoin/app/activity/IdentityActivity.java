package io.ucoin.app.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.internal.experience.ExperienceEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ucoin.app.R;
import io.ucoin.app.adapter.IdentityListAdapter;
import io.ucoin.app.adapter.IdentityViewUtils;
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
import io.ucoin.app.technical.UCoinTechnicalException;

public class IdentityActivity extends ActionBarActivity {

    private static final String TAG = "IdentityActivity";

    public static final String PARAM_IDENTITY = "identity";

    private Button mSignButton;

    private ProgressViewAdapter mProgressViewAdapter;

    private ExpandableListView mWotListView;
    private int mWotLastExpandedPosition = -1;
    private WotExpandableListAdapter mWotExpandableListAdapter;
    private boolean mSignatureSingleLine = true;
    private boolean mPubKeySingleLine = true;

                         @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Wot list
        mWotListView = (ExpandableListView)findViewById(R.id.wot_list_view);
        mWotListView.setVisibility(View.GONE);
        mWotListView.setAdapter(mWotExpandableListAdapter);
        mWotListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // Get certification
                WotCertification cert = (WotCertification) mWotExpandableListAdapter.getChild(groupPosition, childPosition);
                loadIdentity(cert);
                return true;
            }
        });
        mWotListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (mWotLastExpandedPosition != -1
                        && groupPosition != mWotLastExpandedPosition) {
                    mWotListView.collapseGroup(mWotLastExpandedPosition);
                }
                mWotLastExpandedPosition = groupPosition;
            }
        });
        mWotExpandableListAdapter = new WotExpandableListAdapter(this){
            @Override
            public String getGroupText(int groupPosition) {
                // TODO NLS
                return groupPosition == 0 ? "certified by" : "certifiers of";
            }
        };

        // Signature
        final EditText signatureView = (EditText)findViewById(R.id.signature);
        signatureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignatureSingleLine = !mSignatureSingleLine;
                signatureView.setSingleLine(!mSignatureSingleLine);
            }
        });

        // Pub key
        final EditText pubKeyView = (EditText)findViewById(R.id.pubkey);
        pubKeyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPubKeySingleLine = !mPubKeySingleLine;
                pubKeyView.setSingleLine(mPubKeySingleLine);
            }
        });

        View progressView = (View)findViewById(R.id.load_progress);
        progressView.setVisibility(View.VISIBLE);
        mProgressViewAdapter = new ProgressViewAdapter(
                progressView,
                mWotListView);


        mSignButton = (Button)findViewById(R.id.sign_button);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        Identity identity = (Identity)intent.getSerializableExtra(PARAM_IDENTITY);
        if (identity == null) {
            throw new UCoinTechnicalException(String.format("Missing mandatory extra parameter [%s].", PARAM_IDENTITY));
        }

        loadIdentity(identity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_identity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* internal methods */

    protected void loadIdentity(final Identity identity) {
        // Uid
        setTitle(identity.getUid());

        // Timestamp
        EditText timestampView = (EditText)findViewById(R.id.timestamp);
        timestampView.setText(DateUtils.format(identity.getTimestamp()));

        // Signature
        EditText signatureView = (EditText)findViewById(R.id.signature);
        signatureView.setText(identity.getSignature());

        // Pub key
        final EditText pubKeyView = (EditText)findViewById(R.id.pubkey);
        pubKeyView.setText(identity.getPubkey());

        // Sign button
        mSignButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSign(identity);
            }
        });

        // Search user button
        Button transferButton = (Button)findViewById(R.id.transfer_button);
        transferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTransfer(identity);
            }
        });

        // Load WOT data
        mProgressViewAdapter.showProgress(true);
        LoadTask task = new LoadTask(identity);
        task.execute((Void) null);
    }

    protected void doSign(Identity identity) {

        // Disable sign button
        mSignButton.setActivated(false);

        // Execute sign task
        SignTask task = new SignTask(identity);
        task.execute((Void)null);
    }

    protected void doTransfer(final Identity identity) {
        try {
            Intent intent = new Intent(this, TransferActivity.class);
            intent.putExtra(TransferActivity.PARAM_RECEIVER, identity);
            startActivity(intent);
        }
        catch (Throwable t) {
            onError(t);
        }
    }

    protected void onError(Throwable t) {
        Toast.makeText(this,
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
                mWotExpandableListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
                return;
            }

            mWotExpandableListAdapter.setItems(wotCertifications);
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mWotExpandableListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
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
                Toast.makeText(IdentityActivity.this,
                        "Successfully sign " + mIdentity.getUid(),
                        Toast.LENGTH_SHORT).show();
            }
            mProgressViewAdapter.showProgress(false);
        }

        @Override
        protected void onFailed(Throwable t) {
            mWotExpandableListAdapter.setItems(WotExpandableListAdapter.EMPTY_ITEMS);
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
