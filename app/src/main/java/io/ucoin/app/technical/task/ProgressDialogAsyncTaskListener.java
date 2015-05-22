package io.ucoin.app.technical.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import io.ucoin.app.technical.ObjectUtils;

/**
 * Created by eis on 31/03/15.
 */
public class ProgressDialogAsyncTaskListener<Result> implements AsyncTaskListener<Result> {

    public static final String TAG = "AsyncTaskListener";

    private ProgressDialog mProgressDialog;
    private String mProgressMessage = "async task";
    private int mProgressMax;
    private int mProgress;
    private Context mContext;
    private boolean mAutoClose = true;
    private boolean isCancelled = false;

    public ProgressDialogAsyncTaskListener(Context context) {
        ObjectUtils.checkNotNull(context);
        mProgressDialog = new ProgressDialog(context);
        mContext = context;
    }

    public ProgressDialogAsyncTaskListener(ProgressDialog progressDialog) {
        ObjectUtils.checkNotNull(progressDialog);
        mProgressDialog = progressDialog;
        mContext = progressDialog.getContext();
    }

    @Override
    public void onPreExecute() {
        mProgressDialog.show();
    }

    @Override
    public void onSuccess(Object o) {
        if (mAutoClose) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onFailed(Throwable error) {
        if (mAutoClose) {
            mProgressDialog.dismiss();
        }

        Log.d(TAG, "Error during [" + mProgressMessage + "]", error);
        /*Toast.makeText(mProgressDialog.getContext(),
                ExceptionUtils.getMessage(error),
                Toast.LENGTH_LONG)
                .show();*/
    }

    @Override
    public void onCancelled(Result result) {
        if (mAutoClose) {
            mProgressDialog.dismiss();
        }
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
    public void onProgressUpdate() {
        mProgressDialog.setMessage(mProgressMessage);
        if (mProgressDialog.getMax() != mProgressMax) {
            mProgressDialog.setMax(mProgressMax);
        }
        mProgressDialog.setProgress(mProgress);
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    public void setAutoClose(boolean autoCloseProgressDialog) {
        this.mAutoClose = autoCloseProgressDialog;
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
