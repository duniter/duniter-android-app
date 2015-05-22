package io.ucoin.app.adapter;

import android.app.Activity;
import android.view.View;

import io.ucoin.app.technical.ViewUtils;

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

    public ProgressViewAdapter(View parentView, int progressViewId, int viewId) {
        this(parentView.findViewById(progressViewId),
                parentView.findViewById(viewId));
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
     * Shows the progress view and hides the given view.
     */
    public void showProgress(final boolean show) {
        ViewUtils.toogleViews(mView, mProgressView, show, mAnimTime);
    }

    /**
     * Return if the progress view is visible or not
     */
    public boolean isProgressVisible() {
        return mProgressView.getVisibility() == View.VISIBLE;
    }
}
