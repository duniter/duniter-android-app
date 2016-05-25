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
import org.duniter.app.view.InitActivity;
import org.duniter.app.view.connection.pin.FirstPinFragment;

public class FirstConnectionActivity extends Activity {
    private int next;

    private Fragment currentFragment;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setCurrentFragment(InitFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
        if(currentFragment instanceof InitFragment){
            preferences.edit().putBoolean(Application.CONNECTED,false).apply();
            setResult(RESULT_CANCELED);
            finish();
        }else if(currentFragment instanceof FirstPinFragment){
            preferences.edit().putString(Application.PIN, "p").apply();
            setCurrentFragment(InitFragment.newInstance());
        }
    }

    public void setCurrentFragment(Fragment fragment){
        currentFragment = fragment;
        displayFragment(currentFragment);
    }

    public void setNext(int next){
        this.next = next;
        String pin = preferences.getString(Application.PIN,"p");
        if(pin.equals("p")){
            setCurrentFragment(FirstPinFragment.newInstance());
        }else{
            setPin(pin);
        }
    }

    public void setPin(String pin){
        preferences.edit().putString(Application.PIN, pin).apply();
        preferences.edit().putBoolean(Application.CONNECTED,true).apply();

        Intent intent = new Intent(FirstConnectionActivity.this, InitActivity.class);
        intent.putExtra(InitActivity.NEXT_FRAGMENT, next);
        setResult(RESULT_OK, intent);
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