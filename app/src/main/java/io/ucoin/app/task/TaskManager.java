package io.ucoin.app.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import io.ucoin.app.content.Provider;

/**
 * Created by blavenie on 28/08/15.
 */
public class TaskManager {

    private Context mContext;

    public TaskManager(Context context) {
        Provider.initUris(context);
        mContext = context;
    }

    /*public <A extends AsyncTask<?, ?, ?>> A createTask(Class<A> taskClass) {
        return new TaskBuilder(taskClass).build();
    }*/

    /**
     * Helper (Configurer) to create a new task
     */
    public class TaskBuilder<Params, Progress, Result> {

        Class<? extends AsyncTask<Params, Progress, Result>> mTaskClass;

        protected <A extends AsyncTask<Params, Progress, Result>> TaskBuilder(Class<A> taskClass) {
            mTaskClass = taskClass;
        }

        public AsyncTask<Params, Progress, Result> build() {

            try {
                //TODO do not create always, but use a stack ?
                AsyncTask<Params, Progress, Result> task = mTaskClass.newInstance();

                return task;
            } catch(IllegalAccessException e1) {
                //TODO
            } catch(InstantiationException e2) {
                //TODO
            }
            return null;
        }
    }
}
