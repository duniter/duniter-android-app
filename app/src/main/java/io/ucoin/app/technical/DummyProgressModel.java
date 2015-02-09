package io.ucoin.app.technical;

/**
 * A ProgressModel implement, that do nothing. Useful in service implementation, called without ProgressionModel args.
 * Created by eis on 09/02/15.
 */
public class DummyProgressModel implements ProgressModel {
    @Override
    public void setMax(int total) {

    }

    @Override
    public void setProgress(int progression) {

    }

    @Override
    public void increment() {

    }

    @Override
    public void setMessage(String message) {

    }

    @Override
    public void increment(String message) {

    }
}
