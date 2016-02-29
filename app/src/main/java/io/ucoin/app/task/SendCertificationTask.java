package io.ucoin.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.ucoin.app.R;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.document.Certification;
import io.ucoin.app.model.document.SelfCertification;
import io.ucoin.app.model.http_api.WotLookup;
import io.ucoin.app.technical.crypto.AddressFormatException;

/**
 * Created by naivalf27 on 12/02/16.
 */
public class SendCertificationTask extends AsyncTask<Void, Integer, Void> {

    protected UcoinWallet wallet;
    protected WotLookup wotLookup;
    protected RequestQueue queue;
    protected UcoinEndpoint endpoint;
    protected String publicKey;
    protected SelfCertification selfCertification;
    protected Certification certification;
    protected Context context;

    public SendCertificationTask(Context context, UcoinWallet wallet, String publicKey) {
        this.queue = Volley.newRequestQueue(context);
        this.wallet = wallet;
        this.publicKey = publicKey;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        int socketTimeout = 2000;//2 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        endpoint = wallet.currency().peers().at(0).endpoints().at(0);

        StringRequest requestFindLookup = requestFindLookup();
        requestFindLookup.setTag("TAG");
        requestFindLookup.setRetryPolicy(policy);
        queue.add(requestFindLookup);

        return null;
    }

    public StringRequest requestFindLookup(){
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/lookup/" + publicKey;
        return new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                wotLookup = WotLookup.fromJson(response);
                findCertification();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cancelQueue();
            }
        });
    }

    public StringRequest requestSendCertification(final SelfCertification selfCertification, final Certification certification){
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/wot/add/";
        return new StringRequest(
                Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                Toast.makeText(context,context.getString(R.string.certification_sent),Toast.LENGTH_LONG).show();
                cancelQueue();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cancelQueue();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("pubkey", publicKey);
                params.put("self", selfCertification.toString());
                params.put("other", certification.inline());
                return params;
            }
        };
    }

    private void cancelQueue() {
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    private void findCertification(){
        selfCertification = new SelfCertification();
        selfCertification.uid=wotLookup.results[0].uids[0].uid;
        selfCertification.timestamp=wotLookup.results[0].uids[0].meta.timestamp;
        selfCertification.signature=wotLookup.results[0].uids[0].self;

        UcoinBlock currentBlock = wallet.currency().blocks().currentBlock();

        certification = new Certification();
        certification.selfCertification=selfCertification;
        certification.blockNumber=currentBlock.number();
        certification.blockHash=currentBlock.hash();

        certification.certifierPublicKey=wallet.identity().publicKey();
        certification.certifiedPublicKey=publicKey;

        try {
            certification.certifierSignature=certification.sign(wallet.privateKey());
        } catch (AddressFormatException e) {
            e.printStackTrace();
        }

        StringRequest request = requestSendCertification(selfCertification,certification);
        request.setTag("TAG");
        queue.add(request);
    }
}
