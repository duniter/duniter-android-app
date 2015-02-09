package io.ucoin.app.technical;

/**
 * Created by eis on 09/02/15.
 */
public interface ProgressModel {
    public void setMax(int total);
    public void setProgress(int progression);
    public void increment();
    public void setMessage(String message);
    public void increment(String message);
}
