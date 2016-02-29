package io.ucoin.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.model.sql.sqlite.Currency;

/**
 * Created by naivalf27 on 12/02/16.
 */
public class FindLookupTask extends AsyncTask<Bundle, Integer, Void>{

    protected RequestQueue queue;
    protected Context context;

    private OnTaskFinishedListener mListener;

    protected static String TAG = "tag_find_requierements";
    public static final String SEARCH = "search";
    public static final String CURRENCY_ID = "currencyId";

    protected boolean inUid;

    public FindLookupTask(Context context, boolean uid, OnTaskFinishedListener listener){
        this.queue = Volley.newRequestQueue(context);
        this.context = context;
        this.mListener = listener;
        this.inUid = uid;
    }

    @Override
    protected Void doInBackground(Bundle... args) {

        Long currencyId = args[0].getLong(CURRENCY_ID);
        final String search = args[0].getString(SEARCH);

        int socketTimeout = 2000;//2 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        UcoinCurrency currency = new Currency(context,currencyId);
        UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + search;

        final StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        WotLookup wotLookup = WotLookup.fromJson(response);
                        cancelQueue();
                        for(WotLookup.Result result:wotLookup.results){
                            if(inUid) {
                                if (result.uids[0].uid.equals(search)) {
                                    mListener.find();
                                    return;
                                }
                            }else{
                                if (result.pubkey.equals(search)){
                                    mListener.find();
                                    return;
                                }
                            }
                        }
                        mListener.notFind();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cancelQueue();
                        if(error instanceof NoConnectionError){
                            mListener.onError(error);
                        }else{
                            mListener.notFind();
                        }
                    }
                });
        request.setTag(TAG);
        request.setRetryPolicy(policy);
        queue.add(request);
        return null;
    }

    private void cancelQueue(){
        queue.cancelAll(TAG);
    }

    public interface OnTaskFinishedListener {
        void find();
        void notFind();
        void onError(VolleyError error);
    }
}
