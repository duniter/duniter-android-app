package io.ucoin.app.content;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class UcoinQueue implements RequestQueue.RequestFinishedListener {
    private RequestQueue mQueue;
    private ArrayList<Request> mRequests;

    public UcoinQueue(Context context) {
        mQueue = Volley.newRequestQueue(context);
        mQueue.addRequestFinishedListener(this);
        mRequests = new ArrayList<>();
    }

    public Request add(Request request) {
        request.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequests.add(request);
        return mQueue.add(request);
    }

    @Override
    public void onRequestFinished(Request request) {
        RequestQueue.RequestFinishedListener l = (RequestQueue.RequestFinishedListener) request.getTag();
        if(l != null) {
            l.onRequestFinished(request);
        }
        mRequests.remove(request);
    }

    public int count() {
        return mRequests.size();
    }

    public void addRequestFinishedListener(RequestQueue.RequestFinishedListener listener) {
        mQueue.addRequestFinishedListener(listener);
    }

    public void removeRequestFinishedListener(RequestQueue.RequestFinishedListener listener) {
        mQueue.removeRequestFinishedListener(listener);
    }
}
