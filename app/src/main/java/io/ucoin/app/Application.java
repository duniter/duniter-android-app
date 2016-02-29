package io.ucoin.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class Application extends android.app.Application{

    public static final int ACTIVITY_LOOKUP = 0x1;
    public static final int ACTIVITY_CURRENCY_LIST = 0x2;
    public static final int ACTIVITY_CONNECTION = 0x3;


    public static final String EXTRA_CURRENCY_ID = "currency_id";
    public static final String EXTRA_WALLET_ID = "wallet_id";
    public static final String EXTRA_CONTACT_ID = "contact_id";
    public static final String EXTRA_IDENTITY = "identity";
    public static final String EXTRA_IS_CONTACT = "is_contact";
    public static final String EXTRA_VALUE_AMOUNT = "value_amount";
    public static final String EXTRA_SYNC_OP = "sync_op";
    public static final String IDENTITY_LOOKUP = "lookup_for_identity";

    public static final String IDENTITY_CONTACT = "identity_contact";
    public static final String IDENTITY_PUBLICKEY = "identity_publicKey";
    public static final String IDENTITY_UID = "identity_uid";
    public static final String IDENTITY_NAME = "identity_name";
    public static final String IDENTITY_WALLET_ID = "identity_wallet_id";
    public static final String IDENTITY_CURRENCY_ID = "identity_currency_id";

    public static final String UNIT = "currency_unit";
    public static final String UNIT_DEFAULT = "default_currency_unit";
    public static final int UNIT_CLASSIC = 0;
    public static final int UNIT_DU = 1;
    public static final int UNIT_TIME = 2;

    public static final String DECIMAL = "number_decimal";
    public static final String CONNECTION = "connection";
    public static final String PIN = "pin";

    private static Context mContext;
    private static RequestQueue mRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        //LOAD account
        AccountManager accountManager = AccountManager.get(this);
        android.accounts.Account[] accounts = accountManager
                .getAccountsByType(getString(R.string.ACCOUNT_TYPE));

        if (accounts.length < 1) {
            Account account = new Account(
                    getString(R.string.app_name),
                    getString(R.string.ACCOUNT_TYPE));

            ContentResolver.setSyncAutomatically(account, getString(R.string.AUTHORITY), true);
            ContentResolver.setIsSyncable(account, getString(R.string.AUTHORITY), 1);
            AccountManager.get(this).addAccountExplicitly(account, null, null);

            accounts = accountManager
                    .getAccountsByType(getString(R.string.ACCOUNT_TYPE));
        }

        //initialize Volley request queue
        mRequestQueue = Volley.newRequestQueue(mContext);
        mRequestQueue.start();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mRequestQueue.stop();
    }

    public static RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public static Context getContext() {
        return mContext;
    }

    public static long getCurrentTime() {
        return (long) Math.floor(System.currentTimeMillis() / 1000);
    }

    public static void requestSync() {
        requestSync(new Bundle());

    }

    public static void requestSync(Bundle extras) {
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        AccountManager accountManager = AccountManager.get(mContext);
        android.accounts.Account[] accounts = accountManager
                .getAccountsByType(mContext.getString(R.string.ACCOUNT_TYPE));
        ContentResolver.requestSync(accounts[0], mContext.getString(R.string.AUTHORITY), extras);
    }

    public static void cancelSync() {
        AccountManager accountManager = AccountManager.get(mContext);
        android.accounts.Account[] accounts = accountManager
                .getAccountsByType(mContext.getString(R.string.ACCOUNT_TYPE));
        ContentResolver.cancelSync(accounts[0], mContext.getString(R.string.AUTHORITY));
    }

    public static void hideKeyboard(Activity activity, View view){
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
