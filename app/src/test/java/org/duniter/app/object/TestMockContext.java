package org.duniter.app.object;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.RenamingDelegatingContext;

/**
 * Created by naivalf27 on 22/06/16.
 */
public class TestMockContext extends RenamingDelegatingContext {
    public static final String PREFIX = "test.";


    public TestMockContext(Context context) {
        super(context, PREFIX);
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(PREFIX+name, mode);
    }
}
