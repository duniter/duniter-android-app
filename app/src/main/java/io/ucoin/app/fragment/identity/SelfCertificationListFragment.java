package io.ucoin.app.fragment.identity;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.UcoinUris;
import io.ucoin.app.adapter.SelfCertificationCursorAdapter;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinIdentity;
import io.ucoin.app.model.UcoinSelfCertification;
import io.ucoin.app.model.UcoinSelfCertifications;
import io.ucoin.app.model.document.SelfCertification;
import io.ucoin.app.model.sql.sqlite.Identity;
import io.ucoin.app.model.sql.sqlite.SelfCertifications;
import io.ucoin.app.sqlite.SQLiteTable;


public class SelfCertificationListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, Response.ErrorListener, Response.Listener<String> {

    static public SelfCertificationListFragment newInstance(Long identityId) {
        Bundle newInstanceArgs = new Bundle();
        newInstanceArgs.putLong(BaseColumns._ID, identityId);
        SelfCertificationListFragment fragment = new SelfCertificationListFragment();
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
        return inflater.inflate(R.layout.fragment_self_certification_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SelfCertificationCursorAdapter selfCertificationCursorAdapter
                = new SelfCertificationCursorAdapter(getActivity(), null, 0);
        setListAdapter(selfCertificationCursorAdapter);
        getLoaderManager().initLoader(0, getArguments(), this);
        registerForContextMenu(getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, getResources().getString(R.string.revoke));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Long certificationId = acmi.id;
        Long identityId = getArguments().getLong(BaseColumns._ID);
        UcoinSelfCertifications certifications = new SelfCertifications(getActivity(), identityId);
        actionRevoke(certifications.getById(certificationId));
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_self_certification_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem selfItem = menu.findItem(R.id.action_self);

        if (getListAdapter().getCount() == 0) {
            selfItem.setVisible(true);
        } else {
            selfItem.setVisible(false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_self:
                actionSelf();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Long identityId = args.getLong(BaseColumns._ID);

        String selection = SQLiteTable.SelfCertification.IDENTITY_ID + "=? ";
        String selectionArgs[] = new String[]{
                identityId.toString()
        };

        return new CursorLoader(
                getActivity(),
                UcoinUris.SELF_CERTIFICATION_URI,
                null, selection, selectionArgs,
                SQLiteTable.Source._ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ((SelfCertificationCursorAdapter) this.getListAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((SelfCertificationCursorAdapter) this.getListAdapter()).swapCursor(null);
    }

    public void actionSelf() {
        final UcoinIdentity identity = new Identity(getActivity(), getArguments().getLong(BaseColumns._ID));

        final SelfCertification selfCertification = new SelfCertification();
        selfCertification.uid = identity.uid();
        selfCertification.timestamp = Application.getCurrentTime();
        selfCertification.signature = "";//selfCertification.sign(identity.wallet().privateKey());

        UcoinEndpoint endpoint = identity.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/add/";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                SelfCertificationListFragment.this,
                SelfCertificationListFragment.this) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pubkey", identity.publicKey());
                params.put("self", selfCertification.toString());
                return params;
            }
        };
        request.setTag(this);
        Application.getRequestQueue().add(request);
    }

    public void actionRevoke(UcoinSelfCertification certification) {
        final UcoinIdentity identity = certification.identity();

        final SelfCertification selfCertification = new SelfCertification();
        selfCertification.uid = certification.identity().uid();
        selfCertification.timestamp = certification.timestamp();
        selfCertification.signature = certification.self();
        final String signature;
        signature = "";//selfCertification.revoke(identity.wallet().privateKey());

        UcoinEndpoint endpoint = identity.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/revoke/";

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                SelfCertificationListFragment.this,
                SelfCertificationListFragment.this) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pubkey", identity.publicKey());
                params.put("self", selfCertification.toString());
                params.put("sig", signature);
                return params;
            }
        };
        request.setTag(this);
        Application.getRequestQueue().add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

        if (error instanceof NoConnectionError) {
            Toast.makeText(Application.getContext(),
                    getResources().getString(R.string.no_connection),
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(Application.getContext(), error.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResponse(String response) {
        JSONObject object;
        try {
            object = new JSONObject(response);
        } catch (JSONException e) {
            return;
        }

        if (object.has("result")) {
            Toast.makeText(getActivity(), getResources().getString(R.string.revocation_sent), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.self_certification_sent), Toast.LENGTH_LONG).show();
        }

        Application.requestSync();
    }
}
