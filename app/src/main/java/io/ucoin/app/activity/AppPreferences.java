package io.ucoin.app.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import io.ucoin.app.R;

/**
 * Created by naivalf27 on 22/02/16.
 */
public class AppPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}