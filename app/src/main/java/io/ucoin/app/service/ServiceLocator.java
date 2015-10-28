package io.ucoin.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ucoin.app.service.local.AccountService;
import io.ucoin.app.service.local.BlockchainParametersService;
import io.ucoin.app.service.local.Contact2CurrencyService;
import io.ucoin.app.service.local.ContactService;
import io.ucoin.app.service.local.CurrencyService;
import io.ucoin.app.service.local.MovementService;
import io.ucoin.app.service.local.PeerService;
import io.ucoin.app.service.local.WalletService;
import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.NetworkRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.service.remote.UdRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.UCoinTechnicalException;

public class ServiceLocator extends Service implements Closeable {


    private static final String TAG = "ServiceLocator";

    /**
     * The shared instance of this ServiceLocator.
     */
    private static ServiceLocator instance = new ServiceLocator();

    private final Map<Class<?>, Object> mServiceCache;

    private Boolean mIsCacheLoaded = false;

    protected ServiceLocator() {
        // shouldn't be instantiated
        mServiceCache = new HashMap<Class<?>, Object>();
    }

    public class MyBinder extends Binder {
        public ServiceLocator getService() {
            return ServiceLocator.this;
        }
    }

    private final IBinder mBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        Bundle extras = arg0.getExtras();
        //Log.d("service","onBind");
        // Get messager from the Activity
        if (extras != null) {
            //Log.d("service","onBind with extra");
            //outMessenger = (Messenger) extras.get("MESSENGER");
        }
        return mBinder;
    }

    @Override
    public void close() throws IOException {
        for(Object service: mServiceCache.values()) {
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
    public static void setInstance(ServiceLocator newInstance) {
        instance = newInstance;
    }

    /**
     * Gets the shared instance of this Class
     *
     * @return the shared service locator instance.
     */
    public static ServiceLocator instance() {
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

    public CryptoService getCryptoService() {
        return getService(CryptoService.class);
    }
    public DataContext getDataContext() {
        return getService(DataContext.class);
    }
    public HttpService getHttpService() {
        return getService(HttpService.class);
    }
    public AccountService getAccountService() {
        return getService(AccountService.class);
    }
    public CurrencyService getCurrencyService() {
        return getService(CurrencyService.class);
    }
    public BlockchainParametersService getBlockchainParametersService(){
        return getService(BlockchainParametersService.class);
    }
    public WalletService getWalletService() {
        return getService(WalletService.class);
    }
    public PeerService getPeerService() {
        return getService(PeerService.class);
    }
    public ContactService getContactService() {
        return getService(ContactService.class);
    }
    public Contact2CurrencyService getContact2CurrencyService() {
        return getService(Contact2CurrencyService.class);
    }
    public MovementService getMovementService() {
        return getService(MovementService.class);
    }

    public UdRemoteService getUdRemoteService() {
        return getService(UdRemoteService.class);
    }


    public void loadCaches(Context context, long accountId) {

        synchronized (mIsCacheLoaded) {

            if (!mIsCacheLoaded) {
                // Load currencies cache
                getCurrencyService().loadCache(context, accountId);

                // Load blockchain Parameters cache
                getBlockchainParametersService().loadCache(context);

                // Load peers cache
                getPeerService().loadCache(context, accountId);

                mIsCacheLoaded = true;
            }
        }
    }

    /* -- Internal methods -- */
    protected <S extends BaseService> S getService(Class<S> clazz) {
        if (mServiceCache.containsKey(clazz)) {
            return (S) mServiceCache.get(clazz);
        }
        try {
            S service = (S)clazz.newInstance();
            mServiceCache.put(clazz, service);

            // Call initialization
            service.initialize();

            return service;
        }
        catch (Exception e) {
            throw new UCoinTechnicalException("Could not load service: " + clazz.getName(), e);
        }
    }


}
