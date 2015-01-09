package io.ucoin.app.exception;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import io.ucoin.app.R;

/**
 * Created by eis on 05/01/15.
 */
public class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {


    private static final String TAG = "UncaughtExceptionHandler";

    private Context context;

    public UncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        displayMessageBox("Unexpected error", ex.getMessage());
    }

    protected void displayMessageBox(String title, String message) {

        Log.d(TAG, message);

        AlertDialog.Builder messageBox = new AlertDialog.Builder(this.context);
        messageBox.setTitle(title);
        messageBox.setMessage(message);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
}
