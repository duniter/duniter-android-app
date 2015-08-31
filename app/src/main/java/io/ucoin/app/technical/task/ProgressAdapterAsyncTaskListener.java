package io.ucoin.app.technical.task;

import android.content.Context;

import io.ucoin.app.adapter.ProgressViewAdapter;
import io.ucoin.app.technical.ObjectUtils;

/**
 * Created by eis on 31/03/15.
 */
public class ProgressAdapterAsyncTaskListener<Result> extends AbstractAsyncTaskListener<Result> {

    public static final String TAG = "ProgressAdapterAsyncTaskListener";

    private ProgressViewAdapter mProgressViewAdapter;
    private Context mContext;

    public ProgressAdapterAsyncTaskListener(Context context, ProgressViewAdapter progressViewAdapter) {
        super();
        ObjectUtils.checkNotNull(progressViewAdapter);
        mProgressViewAdapter = progressViewAdapter;
        mContext = context;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
        mProgressViewAdapter.showProgress(true);
    }

    @Override
    public void onSuccess(Object o) {
        mProgressViewAdapter.showProgress(false);
    }

    @Override
    public void onFailed(Throwable error) {
        mProgressViewAdapter.showProgress(false);
    }

    @Override
    public void onCancelled(Result result) {
        mProgressViewAdapter.showProgress(false);
    }

    @Override
    public final void onProgressUpdate() {
    }

    @Override
    public Context getContext() {
        return mContext;
    }

}
