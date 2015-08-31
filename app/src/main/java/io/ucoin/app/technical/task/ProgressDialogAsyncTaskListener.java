package io.ucoin.app.technical.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import io.ucoin.app.R;
import io.ucoin.app.technical.ObjectUtils;

/**
 * Created by eis on 31/03/15.
 */
public class ProgressDialogAsyncTaskListener<Result> extends AbstractAsyncTaskListener<Result> {

    public static final String TAG = "AsyncTaskListener";

    private ProgressDialog mProgressDialog;
    private Context mContext;
    private boolean mAutoClose = true;

    public ProgressDialogAsyncTaskListener(Context context) {
        super();
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
        super.onPreExecute();
        mProgressDialog.setMessage(getMessage());
        mProgressDialog.show();
    }

    @Override
    public void onSuccess(Result o) {
        if (mAutoClose) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onFailed(Throwable error) {
        if (mAutoClose) {
            mProgressDialog.dismiss();
        }

        Log.d(TAG, "Listener: catch an error during task [" + getMessage() + "]", error);
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
    public void onProgressUpdate() {
        mProgressDialog.setMessage(getMessage());
        if (mProgressDialog.getMax() != getMax()) {
            mProgressDialog.setMax(getMax());
        }
        mProgressDialog.setProgress(getProgress());
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    public void setAutoClose(boolean autoCloseProgressDialog) {
        this.mAutoClose = autoCloseProgressDialog;
    }
}
