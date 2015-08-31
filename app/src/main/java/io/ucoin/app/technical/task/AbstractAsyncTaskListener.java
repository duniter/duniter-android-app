package io.ucoin.app.technical.task;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.ucoin.app.R;
import io.ucoin.app.technical.ObjectUtils;

/**
 * Created by eis on 31/03/15.
 */
public abstract class AbstractAsyncTaskListener<Result> implements AsyncTaskListener<Result> {

    public static final String TAG = "AbstractAsyncTaskListener";
    private String mProgressMessage;
    private int mProgressMax;
    private int mProgress;
    private boolean isCancelled = false;

    public AbstractAsyncTaskListener() {
    }

    @Override
    public void onPreExecute() {
        if (getContext() != null) {
            mProgressMessage = getContext().getString(R.string.loading_dots);
        }
    }

    @Override
    public final void setMax(int max) {
        mProgressMax = max;
    }

    public int getMax() {
        return mProgressMax;
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

    public String getMessage() {
        return mProgressMessage;
    }

    public int getProgress() {
        return mProgress;
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
