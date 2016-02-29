package io.ucoin.app.task;

import android.os.AsyncTask;
import android.os.Bundle;

import io.ucoin.app.service.CryptoService;
import io.ucoin.app.service.ServiceLocator;
import io.ucoin.app.technical.crypto.KeyPair;

public class GenerateKeysTask extends AsyncTask<Bundle, Void, KeyPair> {
    private OnTaskFinishedListener mListener;

    public GenerateKeysTask(OnTaskFinishedListener listener) {
        mListener = listener;
    }
    @Override
    protected KeyPair doInBackground(Bundle... args){
        String salt = args[0].getString(("salt"));
        String password = args[0].getString(("password"));
        CryptoService service = ServiceLocator.instance().getCryptoService();
        return service.getKeyPair(salt, password);
    }

    @Override
    public void onPostExecute(KeyPair keyPair) {
        mListener.onTaskFinished(keyPair);
    }

    public interface OnTaskFinishedListener {
        void onTaskFinished(KeyPair keyPair);
    }
}


