package io.ucoin.app.technical.task;

import android.content.Context;

/**
 * Created by eis on 31/03/15.
 */
public class NullAsyncTaskListener<Result> extends NullProgressModel implements AsyncTaskListener<Result> {

    private Context mContext;

    public NullAsyncTaskListener() {
        this.mContext = null;
    }

    public NullAsyncTaskListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onFailed(Throwable error) {
    }

    @Override
    public void onSuccess(Result result) {
    }

    @Override
    public void onCancelled(Result result) {
    }

    @Override
    public void onProgressUpdate() {
    }

    @Override
    public Context getContext() {
        return mContext;
    }
}
