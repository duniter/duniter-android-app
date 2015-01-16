package io.ucoin.app.service;

import io.ucoin.app.technical.UCoinTechnicalException;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {


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
    
    public BlockchainService getBlockchainService() {
        return getService(BlockchainService.class);
    }

    public WotService getWotService() {
        return getService(WotService.class);
    }

    public CryptoService getCryptoService() {
        return getService(CryptoService.class);
    }
    public DataService getDataService() {
        return getService(DataService.class);
    }
    public DataContext getDataContext() {
        return getService(DataContext.class);
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
