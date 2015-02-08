package io.ucoin.app.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ucoin.app.service.remote.BlockchainRemoteService;
import io.ucoin.app.service.remote.NetworkRemoteService;
import io.ucoin.app.service.remote.TransactionRemoteService;
import io.ucoin.app.service.remote.WotRemoteService;
import io.ucoin.app.technical.UCoinTechnicalException;

public class ServiceLocator implements Closeable {


    private static final String TAG = "ServiceLocator";

    /**
     * The shared instance of this ServiceLocator.
     */
    private static ServiceLocator instance = new ServiceLocator();

    private final Map<Class<?>, Object> serviceCache;


    protected ServiceLocator() {
        // shouldn't be instantiated
        serviceCache = new HashMap<Class<?>, Object>();
    }
    
    public void init() {
        
    }

    @Override
    public void close() throws IOException {
        for(Object service: serviceCache.values()) {
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
    public WalletService getWalletService() {
        return getService(WalletService.class);
    }


    /* -- Internal methods -- */
    protected <S extends BaseService> S getService(Class<S> clazz) {
        if (serviceCache.containsKey(clazz)) {
            return (S)serviceCache.get(clazz);
        }
        try {
            S service = (S)clazz.newInstance();
            serviceCache.put(clazz, service);

            // Call initialization
            service.initialize();

            return service;
        }
        catch (Exception e) {
            throw new UCoinTechnicalException("Could not load service: " + clazz.getName(), e);
        }
    }


}
