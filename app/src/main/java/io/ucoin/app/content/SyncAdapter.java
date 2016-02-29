package io.ucoin.app.content;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import io.ucoin.app.BuildConfig;
import io.ucoin.app.model.UcoinCurrencies;
import io.ucoin.app.model.UcoinCurrency;
import io.ucoin.app.model.sql.sqlite.Currencies;

public class SyncAdapter extends AbstractThreadedSyncAdapter implements RequestQueue.RequestFinishedListener {

    private static final int SYNC_WALLET_ID = 10;

    private UcoinQueue mQueue;

    public SyncAdapter(Context context, boolean autoInitialize) {
        this(context, autoInitialize, false);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        mQueue = new UcoinQueue(context);
        mQueue.addRequestFinishedListener(this);
    }

    @Override
    public void onPerformSync(Account androidAccount, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        if (mQueue.count() == 0)

        {
            if (BuildConfig.DEBUG)
                Log.d("SYNCADAPTER", "START______________________________________________________");

            UcoinCurrencies currencies = new Currencies(getContext());
            for (final UcoinCurrency currency : currencies) {
                CurrencyWrapper w = new CurrencyWrapper(mQueue, currency);
                w.start();
            }
        } else {
            if (BuildConfig.DEBUG)
                Log.d("SYNCADAPTER", "ALREADY RUNNING____________________________________________");
        }

    }

    @Override
    public void onRequestFinished(Request request) {
        if (mQueue.count() == 0) {
            if (BuildConfig.DEBUG)
                Log.d("SYNCADAPTER", "END________________________________________________________");
        }
    }

/*
    private void notifyNewCurrency(UcoinCurrency currency) {
        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = new Notification.Builder(getContext())
                .setContentTitle("New currency")
                .setContentText("Currency \"" + currency.name() + "\" succesfully added")
                .setSmallIcon(R.drawable.ic_plus_white_36dp)
                .setAutoCancel(true).build();


        NotificationManager manager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, n);
    }

    private void notifyNewCertification() {
        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = new Notification.Builder(getContext())
                .setContentTitle("New certifications")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.ic_plus_white_36dp)
                .setAutoCancel(true).build();

        NotificationManager manager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, n);
    }
*/

}
