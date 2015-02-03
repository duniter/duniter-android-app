package io.ucoin.app.technical;

import android.os.AsyncTask;

/**
 * Created by eis on 22/12/14.
 */
public abstract class AsyncTaskHandleException<Param, Progress, Result> extends AsyncTask<Param, Progress, Result> {

    private Throwable error;

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
}
