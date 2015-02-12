package io.ucoin.app.adapter;

import android.app.Activity;
import android.view.View;

/**
 * Created by eis on 12/01/15.
 */
public class ProgressViewAdapter {

    private View mView;
    private View mProgressView;
    private int mAnimTime;

    public ProgressViewAdapter(Activity activity, int progressViewId, int viewId) {
        this(activity.findViewById(progressViewId),
                activity.findViewById(viewId));
    }

    public ProgressViewAdapter(View progressView, View view) {
        this(progressView, view, progressView.getResources().getInteger(android.R.integer.config_shortAnimTime));
    }

    public ProgressViewAdapter(View progressView, View view, int animationTime) {
        mView = view;
        mProgressView = progressView;
        mAnimTime = animationTime;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        Views.toogleViews(mView, mProgressView, show, mAnimTime);
    }
}
