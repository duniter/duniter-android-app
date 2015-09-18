package io.ucoin.app.service.remote;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by blavenie on 31/08/15.
 */
public abstract class RemoteServiceConnection implements ServiceConnection {

    private boolean mBound = false;

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        RemoteServiceLocator.MyBinder binder = (RemoteServiceLocator.MyBinder) service;
        mBound = true;

        onServiceLoaded(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mBound = false;
    }

    public boolean isBound() {
        return mBound;
    }

    public abstract void onServiceLoaded(RemoteServiceLocator service);
}
