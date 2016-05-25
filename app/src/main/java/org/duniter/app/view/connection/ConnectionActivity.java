package org.duniter.app.view.connection;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import org.duniter.app.Application;
import org.duniter.app.R;
import org.duniter.app.model.Entity.Currency;
import org.duniter.app.view.InitActivity;
import org.duniter.app.view.connection.pin.PinFragment;


public class ConnectionActivity extends Activity {

    public static final int CONNECTION = 0;
    public static final int INSCRIPTION = 1;
    private int next;

    private Fragment currentFragment;

    private Currency currency;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        next = getIntent().getExtras().getInt(InitActivity.NEXT_FRAGMENT,3);
        switch (next) {
            case CONNECTION:
                currentFragment = ConnectionFragment.newInstance();
                break;
            case INSCRIPTION:
                currentFragment = InscriptionFragment.newInstance();
                break;
            case 3:
                currentFragment = PinFragment.newInstance();
                break;
        }
        displayFragment(currentFragment);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment instanceof PinFragment){
            (preferences.edit()).putBoolean(Application.CONNECTED,false);
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    public void nextFragment(){
        switch (next) {
            case CONNECTION:
                currentFragment = ConnectionFragment.newInstance();
                break;
            case INSCRIPTION:
                currentFragment = InscriptionFragment.newInstance();
                break;
            case 3:
                preferences.edit().putBoolean(Application.CONNECTED,true).apply();
                setResult(RESULT_OK);
                finish();
                break;
        }
    }

    public void setCurrency(Currency currency){
        this.currency = currency;
        Intent intent = new Intent(ConnectionActivity.this, InitActivity.class);
        intent.putExtra(Application.CURRENCY_ID,currency.getId());
        setResult(RESULT_OK,intent);
        finish();
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

}