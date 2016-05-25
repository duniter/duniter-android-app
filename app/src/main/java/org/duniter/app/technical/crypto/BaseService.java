package org.duniter.app.technical.crypto;

/**
 * Created by eis on 10/01/15.
 */
public abstract class BaseService {

    public static final String PROTOCOL_VERSION = "2";

    public BaseService() {
    }

    /**
     * Init bean (e.g. link to another services...)
     */
    public void initialize() {}
}
