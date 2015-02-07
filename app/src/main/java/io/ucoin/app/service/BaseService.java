package io.ucoin.app.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by eis on 10/01/15.
 */
public abstract class BaseService extends Service {

    public static final String PROTOCOL_VERSION = "1";

    public BaseService() {
    }

    /**
     * Init bean (e.g. link to another services...)
     */
    public void initialize() {}

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
