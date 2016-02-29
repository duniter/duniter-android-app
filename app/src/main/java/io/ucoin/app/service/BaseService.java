package io.ucoin.app.service;

/**
 * Created by eis on 10/01/15.
 */
public abstract class BaseService {

    public static final String PROTOCOL_VERSION = "1";

    public BaseService() {
    }

    /**
     * Init bean (e.g. link to another services...)
     */
    public void initialize() {}
}
