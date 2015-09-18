package io.ucoin.app.service.remote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ucoin.app.service.BaseService;
import io.ucoin.app.technical.UCoinTechnicalException;

public class RemoteServiceLocator extends Service implements Closeable {


    private static final String TAG = "RemoteServiceLocator";

    /**
     * The shared instance of this ServiceLocator.
     */
    private static RemoteServiceLocator instance = new RemoteServiceLocator();

    private final Map<Class<?>, Object> mServices;

    protected RemoteServiceLocator() {
        // shouldn't be instantiated
        mServices = new HashMap<Class<?>, Object>();
    }

    public class MyBinder extends Binder {
        public RemoteServiceLocator getService() {
            return RemoteServiceLocator.this;
        }
    }

    private final IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        Bundle extras = arg0.getExtras();
        Log.d(TAG, "onBind");
        // Get messager from the Activity
        if (extras != null) {
            //Log.d("service","onBind with extra");
            //outMessenger = (Messenger) extras.get("MESSENGER");
        }
        return mBinder;
    }

    @Override
    public void close() throws IOException {
        for(Object service: mServices.values()) {
            if (service instanceof Closeable) {
                ((Closeable)service).close();
            }
        }
    }

    /**
     * replace the default shared instance of this Class
     *
     * @param newInstance the new shared service locator instance.
     */
    public static void setInstance(RemoteServiceLocator newInstance) {
        instance = newInstance;
    }

    /**
     * Gets the shared instance of this Class
     *
     * @return the shared service locator instance.
     */
    public static RemoteServiceLocator instance() {
        return instance;
    }

    public BlockchainRemoteService getBlockchainRemoteService() {
        return getService(BlockchainRemoteService.class);
    }
    public TransactionRemoteService getTransactionRemoteService() {
        return getService(TransactionRemoteService.class);
    }
    public NetworkRemoteService getNetworkRemoteService() {
        return getService(NetworkRemoteService.class);
    }
    public WotRemoteService getWotRemoteService() {
        return getService(WotRemoteService.class);
    }
    public UdRemoteService getUdRemoteService() {
        return getService(UdRemoteService.class);
    }

    /* -- Internal methods -- */
    protected <S extends BaseService> S getService(Class<S> clazz) {
        if (mServices.containsKey(clazz)) {
            return (S) mServices.get(clazz);
        }
        try {
            S service = (S)clazz.newInstance();
            mServices.put(clazz, service);

            // Call initialization
            service.initialize();

            return service;
        }
        catch (Exception e) {
            throw new UCoinTechnicalException("Could not load service: " + clazz.getName(), e);
        }
    }


}
