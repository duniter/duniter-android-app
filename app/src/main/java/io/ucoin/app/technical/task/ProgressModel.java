package io.ucoin.app.technical.task;

/**
 * Created by eis on 09/02/15.
 */
public interface ProgressModel {
    public void setMax(int total);
    public void setProgress(int progression);
    public void increment();
    public void increment(int nb);
    public void setMessage(String message);
    public void increment(String message);
    public boolean isCancelled();
    public void cancel();
}
