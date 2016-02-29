package io.ucoin.app.content;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.model.UcoinBlock;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.UcoinEndpoint;
import io.ucoin.app.model.UcoinWallet;
import io.ucoin.app.model.http_api.BlockchainBlock;
import io.ucoin.app.model.http_api.BlockchainWithUd;

public class CurrencyWrapper implements
        Response.ErrorListener,
        RequestQueue.RequestFinishedListener {
    private UcoinQueue mRequestQueue;
    private UcoinCurrency mCurrency;

    CurrencyWrapper(UcoinQueue queue, UcoinCurrency currency) {
        mRequestQueue = queue;
        mCurrency = currency;
    }

    public void start() {
        fetchCurrentBlock();
    }

    private void fetchCurrentBlock() {
        UcoinEndpoint endpoint = mCurrency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/current/";
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        BlockchainBlock block = BlockchainBlock.fromJson(response);
                        onCurrentBlockRequest(block);
                    }
                }, null);
        request.setTag(this);
        mRequestQueue.add(request);
    }

    private void fetchUdBlocksNumber() {
        UcoinEndpoint endpoint = mCurrency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/with/ud";
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        BlockchainWithUd udBlocksNumber = BlockchainWithUd.fromJson(response);
                        onUdBlocksNumberRequest(udBlocksNumber);
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
    }

    private void fetchBlock(Long number) {
        UcoinEndpoint endpoint = mCurrency.peers().at(0).endpoints().at(0);
        String url = "http://" + endpoint.ipv4() + ":" + endpoint.port() + "/blockchain/block/" + number;
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        BlockchainBlock block = BlockchainBlock.fromJson(response);
                        onBlockRequest(block);
                    }
                }, this);
        request.setTag(this);
        mRequestQueue.add(request);
    }

    private void onCurrentBlockRequest(BlockchainBlock block) {
        UcoinBlock currentBlock = mCurrency.blocks().currentBlock();
        if (mCurrency.blocks().add(block) != null) {

            if (currentBlock == null || block.number - currentBlock.number() > 1) {
                fetchUdBlocksNumber();
            }

            if (currentBlock != null)
                currentBlock.remove();
        }

        for (UcoinWallet wallet : mCurrency.wallets()) {
            WalletWrapper w = new WalletWrapper(mRequestQueue, wallet);
            if (wallet.syncBlock() < mCurrency.blocks().currentBlock().number()) {
                w.start();
            }
            if (wallet.identity() != null &&
                    wallet.identity().syncBlock() < mCurrency.blocks().currentBlock().number()) {
                IdentityWrapper iw = new IdentityWrapper(mRequestQueue, wallet.identity());
                iw.start();
            }
        }



    }

    private void onUdBlocksNumberRequest(BlockchainWithUd udBlocksNumber) {
        UcoinBlock lastUdBlock = mCurrency.blocks().lastUdBlock();
        for (Long number : udBlocksNumber.result.blocks) {
            if (lastUdBlock == null || number > lastUdBlock.number()) {
                fetchBlock(number);
            }
        }
    }

    private void onBlockRequest(BlockchainBlock block) {
        mCurrency.blocks().add(block);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (BuildConfig.DEBUG) Log.d("CurrencyWrapper", error.toString());
    }

    @Override
    public void onRequestFinished(Request request) {

    }
}

