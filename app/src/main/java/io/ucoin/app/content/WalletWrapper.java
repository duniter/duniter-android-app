package io.ucoin.app.content;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.http_api.TxHistory;
import io.ucoin.app.model.http_api.TxSources;
import io.ucoin.app.model.http_api.UdHistory;

public class WalletWrapper implements Response.ErrorListener, RequestQueue.RequestFinishedListener {
    private UcoinQueue mRequestQueue;
    private UcoinWallet mWallet;
    private HashMap<Request, Boolean> mRequests;

    public WalletWrapper(UcoinQueue queue, UcoinWallet wallet) {
        mRequestQueue = queue;
        mWallet = wallet;
        mRequests = new HashMap<>();
    }

    public void start() {
        if(mWallet.syncBlock() < mWallet.currency().blocks().currentBlock().number()) {
            mRequests.put(fetchSources(), null);
            mRequests.put(fetchUds(), null);
        }
        mRequests.put(fetchTxs(), null);
    }

    public Request fetchSources() {
        UcoinEndpoint endpoint = mWallet.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/tx/sources/";
        url += mWallet.publicKey();

        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mWallet.sources().set(TxSources.fromJson(response));
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }
    public Request fetchTxs() {
        UcoinEndpoint endpoint = mWallet.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/tx/history/";
        url += mWallet.publicKey();
/*
        UcoinTx lastTx = mWallet.txs().getLastConfirmedTx();
        if (lastTx != null) {
            url += "/times/" + lastTx.time() + 1 + "/" + Application.getCurrentTime();
        }
  */
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mWallet.txs().add(TxHistory.fromJson(response));
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }

    public Request fetchUds() {
        UcoinEndpoint endpoint = mWallet.currency().peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/ud/history/";
        url += mWallet.publicKey();
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        UdHistory history = UdHistory.fromJson(response);
                        for (UdHistory.Ud ud : history.history.history) {
                            mWallet.uds().add(ud);
                        }
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
        return request;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (BuildConfig.DEBUG) Log.d("WalletWrapper", error.toString());
    }

    @Override
    public void onRequestFinished(Request request) {
        if (request.hasHadResponseDelivered()) {
            mRequests.put(request, true);
        } else {
            mRequests.put(request, false);
        }

        //requests are all finished
        if (!mRequests.containsValue(null)) {
            // all operations are success
            if (!mRequests.containsValue(false)) {
                mWallet.setSyncBlock(mWallet.currency().blocks().currentBlock().number());
            }
        }
    }
}