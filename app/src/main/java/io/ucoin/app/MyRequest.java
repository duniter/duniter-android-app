package io.ucoin.app;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.model.http_api.WotRequirements;
import io.ucoin.app.model.sql.sqlite.Currency;

/**
 * Created by naivalf27 on 15/02/16.
 */
public class MyRequest implements Response.ErrorListener{

    private static final int TIMEOUT = 2000;


    private Long currencyId;
    private String publicKey;
    private Context context;

    private UcoinCurrency currency;
    private UcoinEndpoint endpoint;
    private RetryPolicy policy;

    private String baseUrl;

    public MyRequest(Context context, Long currencyId, String publicKey) {
        this.policy = new DefaultRetryPolicy(TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        this.currencyId = currencyId;
        this.publicKey = publicKey;
        this.context = context;
        this.currency = new Currency(context,currencyId);
        this.endpoint = this.currency.peers().at(0).endpoints().at(0);
        this.baseUrl = "http://" + endpoint.ipv4() + ":" + endpoint.port();
    }

    public StringRequest getTxHistory(final ResponseRequest r){
        String url = baseUrl + "/tx/history/" + publicKey;

        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        final TxHistory history = TxHistory.fromJson(response);
                        r.onResponse(history);
                    }
                },this);

        request.setTag("TAG");
        request.setRetryPolicy(policy);
        return request;
    }

    public StringRequest getWotRequierements(final ResponseRequest r){
        String url = baseUrl + "/wot/requirements/" + publicKey;

        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                WotRequirements wotRequirements = WotRequirements.fromJson(response);
                r.onResponse(wotRequirements);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                r.onError(error);
            }
        });

        request.setTag("TAG");
        request.setRetryPolicy(policy);
        return request;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
    }

    public interface ResponseRequest{
        void onResponse(Object object);
        void onError(VolleyError error);
    }
}
