package io.ucoin.app.fragment.identity;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.adapter.MembershipCursorAdapter;
import io.ucoin.app.enumeration.MembershipType;
import io.ucoin.app.fragment.dialog.SelectSelfDialogFragment;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinSelfCertification;
import io.ucoin.app.model.document.Membership;
import io.ucoin.app.sqlite.SQLiteTable;
import io.ucoin.app.sqlite.SQLiteView;


public class MembershipListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        Response.Listener<String>,
        Response.ErrorListener {

    private Cursor mCursor;

    static public MembershipListFragment newInstance(Long identityId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, identityId);
        MembershipListFragment fragment = new MembershipListFragment();
        fragment.setArguments(newInstanceArgs);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_membership_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MembershipCursorAdapter membershipCursorAdapter
                = new MembershipCursorAdapter(getActivity(), null, 0);
        setListAdapter(membershipCursorAdapter);
        getLoaderManager().initLoader(0, getArguments(), this);
        getLoaderManager().initLoader(1, getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_membership_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem joinItem = menu.findItem(R.id.action_join);
        MenuItem renewItem = menu.findItem(R.id.action_renew);
        MenuItem leaveItem = menu.findItem(R.id.action_leave);

        if(mCursor == null || mCursor.isClosed() || mCursor.getCount() == 0) {
            joinItem.setVisible(false);
            renewItem.setVisible(false);
            leaveItem.setVisible(false);
            return;
        }

        mCursor.moveToFirst();
        int selfCount = mCursor.getInt(mCursor.getColumnIndex(SQLiteView.Identity.SELF_COUNT));
        boolean isMember = Boolean.valueOf(mCursor.getString(mCursor.getColumnIndex(SQLiteView.Identity.IS_MEMBER)));

        if(selfCount == 0) {
            joinItem.setVisible(false);
            renewItem.setVisible(false);
            leaveItem.setVisible(false);
        } else {
            if (!isMember) {
                joinItem.setVisible(true);
                renewItem.setVisible(false);
                leaveItem.setVisible(false);
            } else {
                joinItem.setVisible(false);
                renewItem.setVisible(true);
                leaveItem.setVisible(true);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_join:
            case R.id.action_renew:
                actionJoin();
                return true;
            case R.id.action_leave:
                actionLeave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Long identityId = args.getLong(BaseColumns._ID);
        if (id == 0) {
            String selection = SQLiteTable.Membership.IDENTITY_ID + "=?";
            String selectionArgs[] = new String[]{
                    identityId.toString()
            };

            return new CursorLoader(
                    getActivity(),
                    UcoinUris.MEMBERSHIP_URI,
                    null, selection, selectionArgs,
                    SQLiteView.Membership.TIME + " DESC");
        } else {
            String selection = SQLiteTable.Identity._ID + "=?";
            String selectionArgs[] = new String[]{
                    identityId.toString()
            };

            return new CursorLoader(
                    getActivity(),
                    UcoinUris.IDENTITY_URI,
                    null, selection, selectionArgs,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 0) {
            ((MembershipCursorAdapter) this.getListAdapter()).swapCursor(data);
        } else {
            mCursor = data;
            setHasOptionsMenu(true);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((MembershipCursorAdapter) this.getListAdapter()).swapCursor(null);
    }

    public void actionJoin() {
        Long identityId = getArguments().getLong(BaseColumns._ID);
        UcoinIdentity identity = new io.ucoin.app.model.sql.sqlite.Identity(getActivity(), identityId);

        if (identity.sigDate() == null) {
            if (identity.selfCount() == 1) {
                Iterator it = identity.selfCertifications().iterator();
                UcoinSelfCertification certification = (UcoinSelfCertification) it.next();
                identity.setSigDate(certification.timestamp());
            } else if (identity.selfCount() > 1) {
                SelectSelfDialogFragment fragment = SelectSelfDialogFragment.newInstance(getArguments().getLong(BaseColumns._ID));
                fragment.setTargetFragment(this, 1);
                fragment.show(getFragmentManager(),
                        fragment.getClass().getSimpleName());

                return;
            } else {
                return;
            }
        }

        createMembership(MembershipType.IN);
    }

    public void actionLeave() {
        createMembership(MembershipType.OUT);
    }

    public void createMembership(MembershipType type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.membership);
        if (type == MembershipType.IN) {
            builder.setMessage(R.string.join_currency);
        } else {
            builder.setMessage(R.string.leave_currency);
        }
        Long identityId = getArguments().getLong(BaseColumns._ID);
        final UcoinIdentity identity = new io.ucoin.app.model.sql.sqlite.Identity(getActivity(), identityId);
        final Membership membership = new Membership();
        membership.currency = identity.currency().name();
        membership.issuer = identity.publicKey();
        UcoinBlock lastBlock = identity.currency().blocks().currentBlock();
        membership.block = lastBlock.number();
        membership.hash = lastBlock.hash();
        membership.membershipType = type;
        membership.UID = identity.uid();
        membership.certificationTs = identity.sigDate();

        //todo prompt for password
        membership.signature = "";//membership.sign(identity.wallet().privateKey());

        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                UcoinEndpoint endpoint = identity.currency().peers().at(0).endpoints().at(0);
                String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/membership/";

                StringRequest request = new StringRequest(
                        Request.Method.POST,
                        url,
                        MembershipListFragment.this,
                        MembershipListFragment.this) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("membership", membership.toString());
                        return params;
                    }
                };
                request.setTag(this);
                Application.getRequestQueue().add(request);
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(Application.getContext(),
                    getResources().getString(R.string.no_connection),
                    Toast.LENGTH_LONG).show();
        } else {
            String str = new String(error.networkResponse.data, Charset.forName("UTF-8"));
            Toast.makeText(Application.getContext(), str, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response) {
        Toast.makeText(getActivity(), getResources().getString(R.string.membership_sent), Toast.LENGTH_LONG).show();
        Application.requestSync();
    }
}
