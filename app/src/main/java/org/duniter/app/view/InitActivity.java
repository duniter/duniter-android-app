package org.duniter.app.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.duniter.app.Application;
import org.duniter.app.model.EntitySql.CurrencySql;
import org.duniter.app.services.ContactService;
import org.duniter.app.services.SqlService;
import org.duniter.app.view.connection.ConnectionActivity;
import org.duniter.app.view.connection.FirstConnectionActivity;

/**
 * Created by naivalf27 on 05/04/16.
 */
public class InitActivity extends Activity {

    public static final String NEXT_FRAGMENT = "next_fragment";
    private SharedPreferences preferences;

    private int nextFragment = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        findActivity();
    }

    public void findActivity(){
        if(isFirstConnection()){
            if(preferences.getBoolean(Application.FIRST_CONNECTION,true)) {
                startFirstConnectionActivity();
            }else {
                startConnectionActivity();
            }
        }else {
            if(pinCodeValide()) {
                ContactService contactService = new ContactService(this);
                contactService.execute(getContentResolver());
                long currencyId = preferences.getLong(Application.CURRENCY_ID, Application.LONG_NULL);
                if(currencyId == Application.LONG_NULL){
                    Toast.makeText(this, "Error InitActivity findActivity", Toast.LENGTH_LONG).show();
                }else {
                    startMainActivity(currencyId);
                }
            }else {
                nextFragment = 3;
                startConnectionActivity();
            }
        }
    }

    private boolean isFirstConnection(){
        boolean result = true;
        Cursor cursor = SqlService.getCurrencySql(this).query(null,null);
        if (cursor.moveToFirst()){
            result = false;
            long currencyId = preferences.getLong(Application.CURRENCY_ID, Application.LONG_NULL);
            if(currencyId == Application.LONG_NULL) {
                preferences.edit().putLong(Application.CURRENCY_ID, cursor.getLong(cursor.getColumnIndex(CurrencySql.CurrencyTable._ID))).apply();
            }
        }
        cursor.close();
        return result;
    }

    private boolean pinCodeValide(){
        return preferences.getBoolean(Application.CONNECTED,false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        if (resultCode != RESULT_OK) {
            if(pinCodeValide()){
                if(isFirstConnection()){
                    editor.putBoolean(Application.FIRST_CONNECTION,true).apply();
                    findActivity();
                }else{
                    finish();
                }
            }else{
                finish();
            }
            return;
        }

        Long currencyId;
        switch (requestCode){
            case Application.ACTIVITY_CONNECTION:
                if(preferences.getLong(Application.CURRENCY_ID,-3) == (long)-3){
                    if(intent!=null) {
                        currencyId = intent.getExtras().getLong(Application.CURRENCY_ID);
                        editor.putLong(Application.CURRENCY_ID, currencyId).apply();
                    }else{
                        editor.putBoolean(Application.FIRST_CONNECTION,true).apply();
                    }
                }
                findActivity();
                break;
            case Application.ACTIVITY_FIRST_CONNECTION:
                if(intent!=null) {
                    nextFragment = intent.getExtras().getInt(InitActivity.NEXT_FRAGMENT);
                }
                editor.putBoolean(Application.FIRST_CONNECTION, false);
                editor.apply();
                findActivity();
                break;
        }
    }

    public void startFirstConnectionActivity() {
        Intent intent = new Intent(this, FirstConnectionActivity.class);
        startActivityForResult(intent, Application.ACTIVITY_FIRST_CONNECTION);
    }

    public void startConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra(InitActivity.NEXT_FRAGMENT,nextFragment);
        startActivityForResult(intent, Application.ACTIVITY_CONNECTION);
    }

    public void startMainActivity(Long currencyId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Application.CURRENCY_ID, currencyId);
        startActivity(intent);
        finish();
    }

}