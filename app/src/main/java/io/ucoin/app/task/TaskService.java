package io.ucoin.app.task;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class TaskService extends Service {

    // Storage for an instance of the sync adapter
    private static TaskManager sTaskManager = null;
    // Object to use as a thread-safe lock
    private static final Object sTaskManagerLock = new Object();

    @Override
    public void onCreate() {
/*
         * Create the task manager as a singleton.
         * Set the task manager as syncable
         * Disallow parallel syncs
         */
        synchronized (sTaskManagerLock) {
            if (sTaskManager == null) {
                sTaskManager = new TaskManager(getApplicationContext());
            }
        }
    }

    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        //return sTaskManager.newBinder();
        return new Binder() {
                // Do something with the task manager ?
        };
    }
}