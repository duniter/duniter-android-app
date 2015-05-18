package io.ucoin.app.technical;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by eis on 12/02/15.
 */
public class ViewUtils {

    private static long mAnimTime = -1;

    protected ViewUtils() {
        // helper class, so protected constructor
    }

    /**
     * Shows the view2 and hides view1.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void toogleViews(final View view1, final View view2) {
        initAnimTime(view1);
        toogleViews(view1, view2, true/*showView2*/, mAnimTime);
    }

    /**
     * Toogle views. if <code>showView2</code> is <code>true</code>, then shows the view2 and hides view1.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void toogleViews(final View view1, final View view2, final boolean showView2) {
        initAnimTime(view1);
        toogleViews(view1, view2, showView2, mAnimTime);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void toogleViews(final View view1, final View view2, final boolean showView2, long animTime) {
        initAnimTime(view1);

        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {

            view1.animate().setDuration(animTime).alpha(
                    showView2 ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view1.setVisibility(showView2 ? View.GONE : View.VISIBLE);
                }
            });

            view2.setVisibility(showView2 ? View.VISIBLE : View.GONE);
            view2.animate().setDuration(animTime).alpha(
                    showView2 ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view2.setVisibility(showView2 ? View.VISIBLE : View.GONE);
                }
            });
        }

        else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            view2.setVisibility(showView2 ? View.VISIBLE : View.GONE);
            view1.setVisibility(showView2 ? View.GONE : View.VISIBLE);
        }
    }

    public static void showKeyboard(Activity activity) {

        // Hide the keyboard, in case we come from imeDone)
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);

    }

    public static void hideKeyboard(Activity activity) {

        // Hide the keyboard, in case we come from imeDone)
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == activity.getCurrentFocus())
                        ? null
                        : activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS|InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static long getShortAnimTime() {
        return mAnimTime;
    }

    /* -- Internal methods -- */

    // Make sure the anim time is load once
    private static void initAnimTime(View view){
        if (mAnimTime > 0) {
            return;
        }
        mAnimTime = view.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }
}
