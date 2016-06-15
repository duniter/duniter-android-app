package org.duniter.app;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import org.duniter.app.view.dialog.preference.ListPreference;
import org.duniter.app.view.dialog.preference.NumberPickerPreference;

/**
 * Created by naivalf27 on 22/02/16.
 */
public class AppPreferences extends PreferenceActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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
    }

}