package io.ucoin.app.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import io.ucoin.app.Application;
import io.ucoin.app.R;
import io.ucoin.app.fragment.connection.ConnectionFragment;
import io.ucoin.app.fragment.connection.FirstPinFragment;
import io.ucoin.app.fragment.connection.InitFragment;
import io.ucoin.app.fragment.connection.InscriptionFragment;
import io.ucoin.app.fragment.connection.PinFragment;
import io.ucoin.app.model.sql.sqlite.Currencies;

/**
 * Created by naivalf27 on 19/02/16.
 */
public class ConnectionActivity extends Activity implements
        InitFragment.FinishAction,
        PinFragment.FinishAction,
        FirstPinFragment.FinishAction,
        ConnectionFragment.FinishAction, InscriptionFragment.FinishAction{

    public static final int CONNECTION = 0;
    public static final int INSCRIPTION = 1;
    private int next;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        int size = new Currencies(this).count();
        if(size>0){
            currentFragment = PinFragment.newInstance();
        }else{
            currentFragment = InitFragment.newInstance();
        }
        displayFragment(currentFragment);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment instanceof ConnectionFragment ||
                currentFragment instanceof InscriptionFragment ||
                currentFragment instanceof FirstPinFragment){
            currentFragment = InitFragment.newInstance();
            displayFragment(currentFragment);
        }else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void finishFirstConnection(Long currencyId) {
        Intent intent = new Intent(ConnectionActivity.this, MainActivity.class);
        intent.putExtra(Application.EXTRA_CURRENCY_ID, currencyId);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void finishPinConnection() {
        setResult(RESULT_OK);
        finish();
    }

    public void finishInit(int next){
        this.next = next;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pin = preferences.getString(Application.PIN,"p");
        if(pin.equals("p")) {
            displayFirstPinFragment();
        }else{
            finishFirstPinConnection();
        }
    }

    public void displayFirstPinFragment(){
        currentFragment = FirstPinFragment.newInstance();
        displayFragment(currentFragment);
    }

    public void displayConnectionFragment(){
        currentFragment = ConnectionFragment.newInstance();
        displayFragment(currentFragment);
    }

    public void displayInscriptionFragment() {
        currentFragment = InscriptionFragment.newInstance();
        displayFragment(currentFragment);
    }

    private void displayFragment(Fragment fragment){
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(
                        R.animator.delayed_fade_in,
                        R.animator.fade_out,
                        R.animator.delayed_fade_in,
                        R.animator.fade_out)
                .replace(R.id.frame_content, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void finishConnection(Long currencyId) {
        finishFirstConnection(currencyId);
    }

    @Override
    public void finishInscription(Long currencyId) {
        finishFirstConnection(currencyId);
    }

    @Override
    public void finishFirstPinConnection() {
        switch (next){
            case CONNECTION:
                displayConnectionFragment();
                break;
            case INSCRIPTION:
                displayInscriptionFragment();
                break;
        }
    }
}
