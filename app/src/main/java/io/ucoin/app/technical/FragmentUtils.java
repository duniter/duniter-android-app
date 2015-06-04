package io.ucoin.app.technical;

import android.support.v4.app.FragmentManager;

/**
 * Created by eis on 19/03/15.
 */
public class FragmentUtils {

    protected FragmentUtils() {
        // helper class
    }

    public static String getPopBackName(FragmentManager fragmentManager, int popCount) {
        // Retrieve the fragment to pop
        FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(
                fragmentManager.getBackStackEntryCount() - (1 + popCount));
        return backStackEntry.getName();
    }


}
