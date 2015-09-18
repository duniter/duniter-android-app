package io.ucoin.app.technical.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.ucoin.app.technical.UCoinTechnicalException;

/**
 * Created by blavenie on 28/08/15.
 */
public class TaskManager {

    /*
     * Gets the number of available cores
     * (not always the same as the maximum number of cores)
     */
    private static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

    private static int KEEP_ALIVE_TIME = 10; /* 10 min */

    private static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;


    private Context mContext;

    private ThreadPoolExecutor mThreadPoolExecutor;

    // A queue of Runnables
    private final BlockingQueue<Runnable> mWorkQueue;



    public TaskManager(Context context) {
        //Provider.initUris(context);
        mContext = context;
        // Instantiates the queue of Runnables as a LinkedBlockingQueue
        mWorkQueue = new LinkedBlockingQueue<Runnable>();

        mThreadPoolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_CORES,       // Initial pool size
                NUMBER_OF_CORES,       // Max pool size
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mWorkQueue);

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
                throw new UCoinTechnicalException(String.format("Unable to create task [%s]", mTaskClass.getSimpleName()), e1);
            } catch(InstantiationException e2) {
                throw new UCoinTechnicalException(String.format("Unable to create task [%s]", mTaskClass.getSimpleName()), e2);
            }
        }
    }

    public <Params, Progress, Result> AsyncTask<Params, Progress, Result> runInPool(AsyncTask<Params, Progress, Result> task, Params... params) {
        return task.executeOnExecutor(mThreadPoolExecutor, params);
    }

    public <Params, Progress, Result> AsyncTask<Params, Progress, Result> run(AsyncTask<Params, Progress, Result> task, Params... params) {
        return task.execute(params);
    }

    public void cancelAll() {
        Runnable[] runnableArray = mWorkQueue.toArray(new Runnable[mWorkQueue.size()]);
        // Stores the array length in order to iterate over the array
        int len = runnableArray.length;
        /*
         * Iterates over the array of Runnables and interrupts each one's Thread.
         */
        synchronized (this) {
            // Iterates over the array of tasks
            for (int runnableIndex = 0; runnableIndex < len; runnableIndex++) {

                // TODO : retrieve the thread from a AsyncTask ??
                // Gets the current thread
                /*Thread thread = runnableArray[runnableIndex];
                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }*/
            }
        }
    }
}
