package io.ucoin.app.technical.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.ucoin.app.technical.ObjectUtils;

/**
 * Created by eis on 22/12/14.
 */
public abstract class AsyncTaskHandleException<Param, Progress, Result>
        extends AsyncTask<Param, Progress, Result>
        implements ProgressModel {

    public static final String TAG = "AsyncTask";

    private Throwable error;
    private AsyncTaskListener mListener;


    public AsyncTaskHandleException(ProgressBar progressBar, TextView progressText) {
        this.mListener = new ProgressBarAsyncTaskListener(progressBar, progressText);
    }

    public AsyncTaskHandleException(ProgressDialog progressDialog) {
        this.mListener = new ProgressDialogAsyncTaskListener(progressDialog);
    }

    public AsyncTaskHandleException(Context context, boolean useProgressDialog) {
        if (useProgressDialog) {
            this.mListener = new ProgressDialogAsyncTaskListener(context);
        }
        else {
            this.mListener = new NullAsyncTaskListener(context);
        }
    }

    public AsyncTaskHandleException(AsyncTaskListener listener) {
        ObjectUtils.checkNotNull(listener);
        this.mListener = listener;
    }

    public AsyncTaskHandleException(Context context) {
        this.mListener = new NullAsyncTaskListener(context);
    }

    //public AsyncTaskHandleException() {
    //    this.mListener = new NullAsyncTaskListener();
    //}

    public AsyncTaskListener getListener() {
        return mListener;
    }
    public void setListener(AsyncTaskListener listener) {
        mListener = listener;
    }

    public Context getContext() {
        return mListener.getContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mListener.onPreExecute();
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
        mListener.onCancelled(result);
    }

    @Override
    protected final Result doInBackground(Param... params) {
        try {
            return (Result)doInBackgroundHandleException(params);
        } catch(Throwable t) {
            this.error = t;
            return null;
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (isSuccess()) {
            onSuccess(result);

            mListener.onSuccess(result);
        }
        else {
            Log.d(TAG, error.getMessage(), error);
            onFailed(error);
            mListener.onFailed(error);
        }
    }

    protected abstract Result doInBackgroundHandleException(Param... params) throws Exception;

    protected void onSuccess(Result result) {
        // Do nothing by default. Could be override
    }

    protected void onFailed(Throwable error) {
        // Do nothing by default. Could be override
    }

    public Throwable getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    /* -- ProgressModel implementation -- */

    @Override
    public void setMax(int max) {
        mListener.setMax(max);
    }

    @Override
    public void setProgress(int progress) {
        mListener.setProgress(progress);
        publishProgress((Progress)null);
    }

    @Override
    public void increment() {
        mListener.increment();
        publishProgress((Progress)null);
    }

    @Override
    public void increment(String message) {
        mListener.increment(message);
        publishProgress((Progress)null);
    }

    @Override
    public void setMessage(String message) {
        mListener.setMessage(message);
        publishProgress((Progress)null);
    }

    @Override
    public void cancel() {
        super.cancel(true);
        mListener.cancel();
    }

    @Override
    protected void onProgressUpdate(final Progress... values) {
        mListener.onProgressUpdate();
    }

    public final String getString(int resId) {
        return mListener.getContext().getString(resId);
    }

    public final String getString(int resId, Object... formatArgs) {
        return mListener.getContext().getString(resId, formatArgs);
    }
}
