package io.ucoin.app.task;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.http_api.TxHistory;

/**
 * Created by naivalf27 on 12/02/16.
 */
public class RefreshWalletTask extends AsyncTask<Void, Integer, Void>{

    protected RequestQueue queue;
    protected Context context;
    protected ArrayList<UcoinWallet> wallets;
    protected Fragment fragment;
    protected int position =0;
    protected RefreshWalletTask myTask;

    protected static String TAG = "tag_refresh_wallet";

    public RefreshWalletTask(Context context, Fragment fragment){
        this.queue = Volley.newRequestQueue(context);
        this.context = context;
        this.wallets = new ArrayList<>();
        this.fragment = fragment;
        this.myTask = this;
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        int socketTimeout = 2000;//2 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        for(UcoinWallet wallet:this.wallets){
            StringRequest request = getRequest(wallet);
            request.setTag(TAG);
            request.setRetryPolicy(policy);
            queue.add(request);
        }
        return null;
    }

    public StringRequest getRequest(final UcoinWallet wallet){
        UcoinEndpoint endpoint = wallet.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/tx/history/" + wallet.publicKey();

        return new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        position++;
                        final TxHistory history = TxHistory.fromJson(response);
                        wallet.txs().add(history);
                        if(position>=wallets.size()){
                            finish();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        position++;
                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                        if(position>=wallets.size()){
                            finish();
                        }
                    }
                });
    }

    public void finish(){
        if(fragment instanceof ActionRefreshWalletTask) {
            ((ActionRefreshWalletTask) fragment).afterRefreshWallet();
        }
        cancelQueue();
    }

    public void addWallet(UcoinWallet wallet){
        this.wallets.add(wallet);
    }

    public void cancelQueue(){
        queue.cancelAll(TAG);
    }

    public interface ActionRefreshWalletTask{
        void afterRefreshWallet();
    }

}
