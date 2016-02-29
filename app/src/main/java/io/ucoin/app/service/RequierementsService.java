package io.ucoin.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
 * Created by naivalf27 on 17/02/16.
 */
public class RequierementsService extends Service {
    private static final String TAG = "RequierementsService";
    public static final int DELAY_UPDATE = 1800000;// 30 minutes


    public static final String BROADCAST_ACTION = "io.ucoin.app.service.requierementsevent";

    public static final String CURRENCY_ID = "currencyId";
    public static final String PUBLIC_KEY= "publicKey";
    public static final String WOT_REQUIEREMENTS = "wotRequirements";

    private final Handler handler = new Handler();

    private RequestQueue queue;
    private Long currencyId;
    private String publicKey;

    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
        queue = Volley.newRequestQueue(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        queue.cancelAll(TAG);
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); //1 second
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.currencyId = intent.getLongExtra(CURRENCY_ID,0);
        this.publicKey = intent.getStringExtra(PUBLIC_KEY);
        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            request();
            handler.postDelayed(this, DELAY_UPDATE);
        }
    };

    private void request(){
        int socketTimeout = 2000;//2 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        UcoinCurrency currency = new Currency(this,currencyId);
        UcoinEndpoint endpoint = currency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/requirements/" + publicKey;

        final StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        WotRequirements wotRequirements = WotRequirements.fromJson(response);
                        DisplayLoggingInfo(wotRequirements);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
        request.setTag(TAG);
        request.setRetryPolicy(policy);
        queue.add(request);
    }

    private void DisplayLoggingInfo(WotRequirements wotRequirements) {
        intent.putExtra(WOT_REQUIEREMENTS, wotRequirements);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        queue.cancelAll(TAG);
        handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
    }
}
