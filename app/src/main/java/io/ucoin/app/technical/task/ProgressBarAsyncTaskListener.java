package io.ucoin.app.technical.task;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.ucoin.app.technical.ObjectUtils;

/**
 * Created by eis on 31/03/15.
 */
public class ProgressBarAsyncTaskListener<Result> implements AsyncTaskListener<Result> {

    public static final String TAG = "AsyncTaskListener";

    private ProgressBar mProgressBar;
    private TextView mProgressTextView;
    private String mProgressMessage;
    private int mProgressMax;
    private int mProgress;
    private Context mContext;
    private boolean isCancelled = false;


    public ProgressBarAsyncTaskListener(ProgressBar progressBar, TextView progressText) {
        ObjectUtils.checkNotNull(progressBar);
        mProgressBar = progressBar;
        mProgressTextView = progressText;
        mContext = mProgressBar.getContext().getApplicationContext();
    }

    public ProgressBarAsyncTaskListener(ProgressBar progressBar) {
        ObjectUtils.checkNotNull(progressBar);
        mProgressBar = progressBar;
        mProgressTextView = null;
        mContext = mProgressBar.getContext().getApplicationContext();
    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onSuccess(Object o) {
    }

    @Override
    public void onFailed(Throwable error) {
        Log.d(TAG, "Error during [" + mProgressMessage + "]", error);
        /*Toast.makeText(mProgressBar.getContext(),
                ExceptionUtils.getMessage(error),
                Toast.LENGTH_LONG)
                .show();*/
    }

    @Override
    public void onCancelled(Result result) {

    }

    @Override
    public final void setMax(int max) {
        mProgressMax = max;
    }

    @Override
    public final void setProgress(int progress) {
        mProgress = progress;
    }

    @Override
    public final void increment() {
        mProgress++;
    }

    @Override
    public final void increment(String message) {
        mProgress++;
        mProgressMessage = message;
    }

    @Override
    public final void setMessage(String message) {
        mProgressMessage = message;
    }

    @Override
    public final void onProgressUpdate() {
        if (mProgressBar.getMax() != mProgressMax) {
            mProgressBar.setMax(mProgressMax);
        }
        mProgressBar.setProgress(mProgress);

        if (mProgressTextView != null) {
            mProgressTextView.setText(mProgressMessage);
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
}
