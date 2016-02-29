package io.ucoin.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.WotRequirements;
import io.ucoin.app.model.sql.sqlite.Currency;

/**
 * Created by naivalf27 on 12/02/16.
 */
public class FindRequierementsTask extends AsyncTask<Bundle, Integer, Void>{

    protected RequestQueue queue;
    protected Context context;

    private OnTaskFinishedListener mListener;

    protected static String TAG = "tag_find_requierements";

    public FindRequierementsTask(Context context,OnTaskFinishedListener listener){
        this.queue = Volley.newRequestQueue(context);
        this.context = context;
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(Bundle... args) {

        Long currencyId = args[0].getLong(("currencyId"));
        String publicKey = args[0].getString(("publicKey"));

        int socketTimeout = 2000;//2 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        UcoinCurrency currency = new Currency(context,currencyId);
        UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/requirements/" + publicKey;

        final StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        WotRequirements wotRequirements = WotRequirements.fromJson(response);
                        cancelQueue();
                        mListener.onTaskFinished(wotRequirements);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cancelQueue();
                        mListener.onTaskError(error);
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
        void onTaskFinished(WotRequirements requirements);
        void onTaskError(VolleyError error);
    }
}
