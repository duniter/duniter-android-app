package io.ucoin.app.technical.task;

/**
 * Created by eis on 31/03/15.
 */
public class NullProgressModel implements ProgressModel {

    private boolean isCancelled = false;

    @Override
    public void setMax(int max) {
    }

    @Override
    public void setProgress(int progress) {
    }

    @Override
    public void increment() {
    }

    @Override
    public void increment(int nb) {
    }

    @Override
    public void increment(String message) {
    }

    @Override
    public void setMessage(String message) {
    }

    @Override
    public void cancel() {
        isCancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }
}
