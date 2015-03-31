package io.ucoin.app.technical.task;

import android.content.Context;

/**
 * Created by eis on 31/03/15.
 */
public interface AsyncTaskListener<Result> extends ProgressModel {

    void onPreExecute();

    void onSuccess(Result result);

    void onFailed(Throwable error);

    void onCancelled(Result result);

    void onProgressUpdate();

    Context getContext();
}
