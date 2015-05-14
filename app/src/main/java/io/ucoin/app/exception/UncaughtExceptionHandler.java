package io.ucoin.app.exception;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by eis on 05/01/15.
 */
public class UncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private Context context;

    public UncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable error) {
        Log.d(getClass().getSimpleName(), error.getMessage(), error);

        displayMessageBox("Unexpected error", error.getMessage());
    }

    protected void displayMessageBox(final String title, final String message) {

        /*if (context instanceof Activity) {
            final Activity activity = (Activity)context;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    AlertDialog.Builder messageBox = new AlertDialog.Builder(activity);
                    messageBox.setTitle(title);
                    messageBox.setMessage(message);
                    messageBox.setCancelable(false);
                    messageBox.setNeutralButton("OK", null);
                    messageBox.show();

                }
            });
        }
        else {*/
            Toast.makeText(this.context, message, Toast.LENGTH_LONG).show();
        //}
    }
}
