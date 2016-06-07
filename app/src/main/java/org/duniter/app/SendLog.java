package org.duniter.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by naivalf27 on 07/06/16.
 */
public class SendLog extends Activity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature (Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside (false); // prevent users from dismissing the dialog by tapping outside
        setContentView (R.layout.send_log);

        Button yes = (Button) findViewById(R.id.yes);
        yes.setOnClickListener(this);
        Button no = (Button) findViewById(R.id.no);
        no.setOnClickListener(this);
        if (((Application)getApplication()).getHasSendLog()){
            finish();
        }
    }



    @Override
    public void onClick (View v)
    {
        switch (v.getId()){
            case R.id.yes :
                sendLogFile();
                ((Application)getApplication()).setHas_send_log(true);
                finish();
                break;
            case R.id.no :
                ((Application)getApplication()).setHas_send_log(true);
                finish();
                break;
        }
    }

    private void sendLogFile ()
    {
        String fullName = extractLogToFile();
        if (fullName == null){
            Log.e("SEND_LOG","error fullName is null");
            return;
        }

        Intent intent = new Intent (Intent.ACTION_SEND);
        intent.setType ("plain/text");
        intent.putExtra (Intent.EXTRA_EMAIL, new String[] {"applog@tccp.fr"});
        intent.putExtra (Intent.EXTRA_SUBJECT, "Duniter App : log file");
        intent.putExtra (Intent.EXTRA_STREAM, Uri.parse ("file://" + fullName));
        intent.putExtra (Intent.EXTRA_TEXT, "Log file attached."); // do this so some email clients don't complain about empty body.
        startActivity (intent);
        onDestroy();
    }

    private String extractLogToFile()
    {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo (this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = Environment.getExternalStorageDirectory() + "/" + "Duniter_App/";
        File dir = new File(path);
        if(!dir.exists()){
            dir.mkdir();
        }
        String fullName = path + "log";

        // Extract to file.
        File file = new File (fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try
        {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
//            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
//                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
//                    "logcat -d -v time";
            String cmd = "logcat -d -v time FATAL:E";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader (process.getInputStream());

            // write output stream
            writer = new FileWriter (file);
            writer.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
            writer.write ("Device: " + model + "\n");
            writer.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do
            {
                int n = reader.read (buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write (buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        }
        catch (IOException e)
        {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        return fullName;
    }
}
