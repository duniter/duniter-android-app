package org.duniter.app.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.duniter.app.Application;
import org.duniter.app.model.EntitySql.CurrencySql;
import org.duniter.app.services.ContactService;
import org.duniter.app.services.SqlService;
import org.duniter.app.view.connection.ConnectionActivity;
import org.duniter.app.view.connection.PinActivity;

/**
 * create by Naivalf27
 */
public class InitActivity extends Activity {

    public static final int ETAPE_0 = 0;//on ne sais pas si il y a des monnaies en base

    public static final int ETAPE_1_1 = 11;//choix inscription ou connexion
    public static final int ETAPE_1_2 = 12;//saisie du code pin

    public static final int ETAPE_2_1 = 21;//création du code pin
    public static final int ETAPE_2_2 = 22;//saisie du code pin

    public static final int ETAPE_3_1 = 31;//inscription
    public static final int ETAPE_3_2 = 32;//connection

    public static final int ETAPE_4 = 40;//on peut afficher les portefeuilles
    public static final String FUTUR_ETAPE = "futur_etape";
    public static final String PIN = "pin";
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        findAction();
    }

    public void findAction(){
        int etape = preferences.getInt(Application.ETAPE,ETAPE_0);
        switch (etape){
            case ETAPE_0:
                findHaveCurrency();
                break;
            case ETAPE_1_2:
            case ETAPE_2_1:
            case ETAPE_2_2:
                grapCreateCodePin();
                break;
            case ETAPE_1_1:
            case ETAPE_3_1:
            case ETAPE_3_2:
                connectionInscrition();
                break;
            case ETAPE_4:
                launchMainActivity();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putInt(Application.ETAPE,ETAPE_0).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        preferences.edit().putInt(Application.ETAPE,ETAPE_0).apply();
    }

    private void findHaveCurrency(){
        int etapeSuivante;
        Cursor cursor = SqlService.getCurrencySql(this).query(null,null);
        if (cursor.moveToFirst()){
            long currencyId = preferences.getLong(Application.CURRENCY_ID, Application.LONG_NULL);
            if(currencyId == Application.LONG_NULL) {
                preferences.edit().putLong(Application.CURRENCY_ID, cursor.getLong(cursor.getColumnIndex(CurrencySql.CurrencyTable._ID))).apply();
            }
            etapeSuivante = ETAPE_1_2;
        }else{
            etapeSuivante = ETAPE_1_1;
        }
        cursor.close();
        preferences.edit().putInt(Application.ETAPE,etapeSuivante).apply();
        findAction();
    }

    private boolean passwordExist(){
        String pin = preferences.getString(Application.PIN, "p");
        return !pin.equals("p");
    }

    private void grapCreateCodePin(){
        int etape = preferences.getInt(Application.ETAPE,0);

        Intent intent = new Intent(this, PinActivity.class);
        intent.putExtra(Application.ETAPE,etape);
        startActivityForResult(intent, Application.ACTIVITY_PIN);
    }

    private void connectionInscrition(){
        int etape = preferences.getInt(Application.ETAPE,ETAPE_0);
        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra(Application.ETAPE,etape);
        startActivityForResult(intent, Application.ACTIVITY_CONNECTION);
    }

    private void launchMainActivity(){
        ContactService contactService = new ContactService(this);
        contactService.execute(getContentResolver());
        preferences.edit().putInt(Application.ETAPE,ETAPE_0).apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        Long currencyId;

        if (resultCode != RESULT_OK) {
            int etape = preferences.getInt(Application.ETAPE,ETAPE_0);
            int nextEtape;
            switch (etape){
                case ETAPE_1_1:
                case ETAPE_1_2:
                    nextEtape = ETAPE_0;
                    break;
                case ETAPE_2_1:
                case ETAPE_2_2:
                case ETAPE_3_1:
                case ETAPE_3_2:
                    nextEtape = ETAPE_1_1;
                    break;
                default:
                    nextEtape = ETAPE_0;
                    break;
            }
            editor.putInt(Application.ETAPE,nextEtape).apply();
            findAction();
            return;
        }

        int etape = ETAPE_0;
        int previousEtape = intent.getIntExtra(Application.ETAPE,ETAPE_0);
        switch (requestCode) {
            case Application.ACTIVITY_CONNECTION:
                switch (previousEtape) {
                    case ETAPE_1_1:
                        int futurEtape = intent.getIntExtra(FUTUR_ETAPE,ETAPE_0);
                        if (futurEtape!=ETAPE_0){
                            editor.putInt(FUTUR_ETAPE,futurEtape).apply();
                            etape = passwordExist() ? ETAPE_2_2 : ETAPE_2_1;
                        }
                        break;
                    case ETAPE_3_1:
                    case ETAPE_3_2:
                        currencyId = intent.getLongExtra(Application.CURRENCY_ID,0);
                        editor.putLong(Application.CURRENCY_ID,currencyId).apply();
                        etape = currencyId!=0 ? ETAPE_4 : previousEtape;
                        break;
                }
                break;
            case Application.ACTIVITY_PIN:
                editor.putBoolean(Application.CONNECTED,true).apply();
                switch (previousEtape) {
                    case ETAPE_1_2:
                        etape = ETAPE_4;
                        break;
                    case ETAPE_2_2:
                        etape = preferences.getInt(FUTUR_ETAPE,ETAPE_0);
                        break;
                    case ETAPE_2_1:
                        String pin = intent.getStringExtra(InitActivity.PIN);
                        editor.putString(Application.PIN,pin).apply();
                        etape = preferences.getInt(FUTUR_ETAPE,ETAPE_0);
                        break;
                }
                break;
        }
        editor.putInt(Application.ETAPE,etape).apply();
        findAction();
    }

}