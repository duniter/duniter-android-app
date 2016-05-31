package org.duniter.app;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.duniter.app.model.Entity.Currency;
import org.duniter.app.model.Entity.Identity;
import org.duniter.app.model.Entity.Wallet;
import org.duniter.app.model.EntityServices.CurrencyService;
import org.duniter.app.model.EntityServices.WalletService;
import org.duniter.app.services.SqlService;
import org.duniter.app.technical.callback.Callback;

/**
 * Created by naivalf27 on 24/05/16.
 */
public class Sync extends Service {

    private static Timer timer = new Timer();
    private Context context;

    private List<Currency> listCurrency;
    private List<Wallet> listWallet;
    private List<Identity> listIdentity;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new mainTask(), 0, 600000);//10 minute

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer.purge();
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {
            toastHandler.sendEmptyMessage(0);
        }

    }

    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Log.d("UPDATE SERVICE","----------START----------");
            Log.d("UPDATE SERVICE","search currency and wallet");
            try {
                listCurrency = SqlService.getCurrencySql(context).getAllCurrency();
                listWallet = SqlService.getWalletSql(context).getAllWallet();
                listIdentity = SqlService.getIdentitySql(context).getAllIdentity();
            }catch (IllegalStateException e){

            }
            Log.d("UPDATE SERVICE","search update");
            update();
        }
    };

    private void update(){
        //TODO current block
        updateCurrency(0);
    }

    private void updateCurrency(final int position){
        if (listCurrency.size() == 0){
            Log.d("UPDATE SERVICE","----------STOP----------");
            return;
        }
        CurrencyService.updateCurrency(this, listCurrency.get(position), new Callback() {
            @Override
            public void methode() {
                if (position >= listCurrency.size()-1){
                    updateWallet(0);
                }else{
                    updateCurrency(position+1);
                }
            }
        });
    }

    private void updateWallet(final int position){
        if (listWallet.size() == 0){
            Log.d("UPDATE SERVICE","----------STOP----------");
            return;
        }
        WalletService.updateWallet(this, listWallet.get(position), true, new Callback() {
            @Override
            public void methode() {
                if (position < listWallet.size()-1){
                    updateWallet(position+1);
                }else{
                    Log.d("UPDATE SERVICE","----------STOP----------");
                }
            }
        });
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (Sync.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
