package io.ucoin.app.technical;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by eis on 22/12/14.
 */
public abstract class AsyncTaskHandleException<Param, Progress, Result>
        extends AsyncTask<Param, Progress, Result>
        implements ProgressModel {

    private Throwable error;
    private ProgressBar mProgressBar;
    private TextView mProgressTextView;
    private String mProgressMessage;
    private int mProgressMax;
    private int mProgress;


    public AsyncTaskHandleException(ProgressBar progressBar, TextView progressText) {
        mProgressBar = progressBar;
        mProgressTextView = progressText;
    }

    public AsyncTaskHandleException() {
        mProgressBar = null;
        mProgressTextView = null;
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
        }
        else {
            onFailed(error);
        }
    }

    protected abstract Result doInBackgroundHandleException(Param... params) throws Exception;

    protected abstract void onSuccess(Result result);

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
        mProgressMax = max;
    }

    @Override
    public void setProgress(int progress) {
        mProgress = progress;
        publishProgress((Progress)null);
    }

    @Override
    public void increment() {
        mProgress++;
        publishProgress((Progress)null);
    }

    @Override
    public void increment(String message) {
        mProgress++;
        mProgressMessage = message;

        publishProgress((Progress)null);
    }

    @Override
    public void setMessage(String message) {
        mProgressMessage = message;
        publishProgress((Progress)null);
    }

    @Override
    protected void onProgressUpdate(final Progress... values) {
        if (mProgressBar != null) {
            if (mProgressBar.getMax() != mProgressMax) {
                mProgressBar.setMax(mProgressMax);
            }
            mProgressBar.setProgress(mProgress);
        }
        if (mProgressTextView != null) {
            mProgressTextView.setText(mProgressMessage);
        }
    }

}
