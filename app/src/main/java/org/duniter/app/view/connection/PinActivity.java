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
import org.duniter.app.view.connection.pin.CreatePinFragment;
import org.duniter.app.view.connection.pin.PinFragment;

public class PinActivity extends Activity {

    int etapeCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        Fragment fragment = null;

        etapeCurrent = getIntent().getIntExtra(Application.ETAPE,InitActivity.ETAPE_0);
        switch (etapeCurrent){
            case InitActivity.ETAPE_1_2:
            case InitActivity.ETAPE_2_2:
                fragment = PinFragment.newInstance();
                break;
            case InitActivity.ETAPE_2_1:
                fragment = CreatePinFragment.newInstance();
                break;
        }
        displayFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void setPin(String pin){
        Intent intent = new Intent(PinActivity.this, InitActivity.class);
        intent.putExtra(InitActivity.PIN, pin);
        intent.putExtra(Application.ETAPE,etapeCurrent);
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