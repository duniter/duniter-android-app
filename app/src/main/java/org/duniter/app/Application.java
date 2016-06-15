package org.duniter.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.duniter.app.model.EntitySql.TxSql;
import org.duniter.app.services.SqlService;

import java.io.File;
import java.io.FileWriter;

public class Application extends android.app.Application{

    public static final int ACTIVITY_CONNECTION = 1;
    public static final int ACTIVITY_FIRST_CONNECTION = 2;

    public static final String LAST_UPDATE ="last_update";

    public static final String CURRENCY_ID = "currency_id";
    public static final String CONNECTED = "connected";
    public static final long LONG_NULL = -999;
    public static final String FIRST_CONNECTION = "first_connection";
    public static final String PIN = "pin";
    public static final int PROTOCOLE_VERSION = 2;
    public static final String DECIMAL = "decimal";
    public static final String UNIT = "unit";
    public static final int UNIT_CLASSIC = 0;
    public static final String UNIT_DEFAULT = "default_unit";
    public static final int UNIT_DU = 1;
    public static final int UNIT_TIME = 2;
    public static final String CURRENCY = "currency";
    public static final String WALLET_ID = "wallet_id";
    public static final String IDENTITY_ID = "identity_id";
    public static final String PUBLIC_KEY = "public_key";
    public static final String RENEW = "renew_membership";
    public static final String CONTACT = "contact";
    public static final String USE_OBLIVION = "use_oblivion";
    public static final String DISPLAY_DU = "display_du";
    public static final String DELAY_SYNC = "delay_sync";


    private static Context      mContext;

    private boolean has_send_log = false;

    public boolean getHasSendLog(){
        return has_send_log;
    }

    public void setHas_send_log(boolean b){
        has_send_log = b;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        requestSync();

//        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
//        {
//            @Override
//            public void uncaughtException (Thread thread, Throwable e)
//            {
//                handleUncaughtException (thread, e);
//            }
//        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        String ms = e.getMessage() +":\n";
        StackTraceElement[] stack = e.getStackTrace();
        if (stack != null) {
            for (StackTraceElement aStack : stack) {
                ms += aStack.toString()+"\n";
            }
        }
        Log.e("FATAL",ms+"\n");
        Intent intent = new Intent ();
        intent.setAction ("org.duniter.app.SEND_LOG");
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        startActivity (intent);

        System.exit(1);// kill off the crashed app
    }

    public static Context getContext() {
        return mContext;
    }

    public static void requestSync() {
        mContext.startService(new Intent(mContext, Sync.class));
    }

    public static void forcedSync() {
        if (Sync.isRunning(mContext)){
            cancelSync();
            requestSync();
        }
    }

    public static void cancelSync() {
        mContext.stopService(new Intent(mContext, Sync.class));
    }

    public static void hideKeyboard(Activity activity, View view){
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void exportDb() {
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "csvname.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            String request = "SELECT * FROM "+ TxSql.TxTable.TABLE_NAME;
            Cursor cursor = SqlService.getCurrencySql(getContext()).query(request);
            csvWrite.writeNext(cursor.getColumnNames());
            String[] columnNames = null;
            while(cursor.moveToNext())
            {
                //Which column you want to exprort
                columnNames = columnNames==null ? cursor.getColumnNames() : columnNames;
                String[] arrStr = new String[columnNames.length];
                for (int i=0;i<columnNames.length;i++){
                    arrStr[i] = cursor.getString(cursor.getColumnIndex(columnNames[i]));
                }
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            cursor.close();
        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }
}
