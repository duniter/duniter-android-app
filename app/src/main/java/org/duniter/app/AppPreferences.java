package org.duniter.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import org.duniter.app.view.dialog.NumberPickerPreference;

/**
 * Created by naivalf27 on 22/02/16.
 */
public class AppPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        String[] prefs = new String[]{Application.UNIT_DEFAULT,Application.UNIT,Application.DECIMAL,Application.DELAY_SYNC};

        for (String key : prefs) {
            Preference pref = findPreference(key);
            if (pref instanceof ListPreference) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            }else if (pref instanceof NumberPickerPreference){
                NumberPickerPreference numberPickerPreference = (NumberPickerPreference) pref;
                String val = String.valueOf(numberPickerPreference.getValue());
                pref.setSummary(val);
            }
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                onCreate(savedInstanceState);
            }
        });

    }

}