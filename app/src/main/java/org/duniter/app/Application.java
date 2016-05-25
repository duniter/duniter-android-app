package org.duniter.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Application extends android.app.Application{

    public static final int ACTIVITY_CONNECTION = 1;
    public static final int ACTIVITY_FIRST_CONNECTION = 2;

    public static final String LAST_UPDATE ="last_update";

    public static final String CURRENCY_ID = "currency_id";
    public static final String CONNECTED = "connected";
    public static final long LONG_NULL = -999;
    public static final String FIRST_CONNECTION = "first_connection";
    public static final String PIN = "pin";
    public static final int PROTOCOLE_VERSION = 2;
    public static final String DECIMAL = "decimal";
    public static final String UNIT = "unit";
    public static final int UNIT_CLASSIC = 0;
    public static final String UNIT_DEFAULT = "default_unit";
    public static final int UNIT_DU = 1;
    public static final int UNIT_TIME = 2;
    public static final String CURRENCY = "currency";
    public static final String WALLET_ID = "wallet_id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String PUBLIC_KEY = "public_key";
    public static final String RENEW = "renew_membership";
    public static final String CONTACT = "contact";


    private static Context      mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        requestSync();
    }

    public static Context getContext() {
        return mContext;
    }

    public static void requestSync() {
        mContext.startService(new Intent(mContext, Sync.class));
    }

    public static void forcedSync() {
        if (Sync.isRunning(mContext)){
            cancelSync();
            requestSync();
        }
    }

    public static void cancelSync() {
        mContext.stopService(new Intent(mContext, Sync.class));
    }

    public static void hideKeyboard(Activity activity, View view){
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
