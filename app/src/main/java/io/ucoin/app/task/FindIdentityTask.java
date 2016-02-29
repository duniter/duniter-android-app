package io.ucoin.app.task;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.ActionBarActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.model.IdentityContact;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currencies;
import io.ucoin.app.model.sql.sqlite.Currency;

/**
 * Created by naivalf27 on 07/01/16.
 */
public class FindIdentityTask extends AsyncTask<String, Void, String> {

    protected Context mContext;
    protected WotLookup.Result[] results;
    protected String publicKey;
    protected Long currencyId;
    protected RequestQueue queue;
    protected ActionBarActivity activity;

    public FindIdentityTask(Context context, Long currencyId, String publicKey, ActionBarActivity activity){
        this.mContext = context;
        this.publicKey = publicKey;
        this.currencyId = currencyId;
        this.queue = Volley.newRequestQueue(context);
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... param) {
        results = null;
        retrieveIdentities();
        return null;
    }

    protected void retrieveIdentities(){
        int socketTimeout = 2000;//2 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        if (currencyId.equals(Long.valueOf(-1))) {
            UcoinCurrencies currencies = new Currencies(Application.getContext());
            Cursor cursor = currencies.getAll();
            UcoinEndpoint endpoint;
            if (cursor.moveToFirst()) {
                do {
                    Long cId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                    UcoinCurrency currency = new Currency(mContext, cId);
                    endpoint = currency.peers().at(0).endpoints().at(0);
                    String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + this.publicKey;
                    StringRequest request = request(url, cId);
                    request.setTag(this);
                    request.setRetryPolicy(policy);
                    //Application.getRequestQueue().add(request);
                    queue.add(request);
                } while (cursor.moveToNext());
            }
        } else {
            UcoinCurrency currency = new Currency(mContext, currencyId);
            UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
            String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + this.publicKey;
            StringRequest request = request(url, currencyId);
            request.setTag("TAG");
            request.setRetryPolicy(policy);
            //Application.getRequestQueue().add(request);
            queue.add(request);
        }
    }

    public StringRequest request(String url, final Long id){
        final String name=(new Currency(mContext,id)).name();
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        WotLookup lookup = WotLookup.fromJson(response);
                        queue.cancelAll(new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {
                                return true;
                            }
                        });
                        if(activity instanceof SendIdentity){
                            IdentityContact entity = null;
                            if(lookup.results.length!=0) {
                                entity = new IdentityContact(false, "", lookup.results[0].uids[0].uid, publicKey, name, id);
                            }
                            String message ="";
                            if(entity==null){
                                message = mContext.getResources().getString(R.string.no_result_publickey);
                            }
                            ((SendIdentity) activity).send(entity,message);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        queue.cancelAll(new RequestQueue.RequestFilter() {
                            @Override
                            public boolean apply(Request<?> request) {
                                return true;
                            }
                        });
                        if(activity instanceof SendIdentity){
                            String message = mContext.getResources().getString(R.string.no_connection);
                            ((SendIdentity) activity).send(null,message);
                        }
                    }
                });
        return request;
    }

    public interface SendIdentity{
        void send(IdentityContact entity, String message);
    }
}
