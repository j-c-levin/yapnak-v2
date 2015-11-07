package com.uq.yapnak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    final Context context = Settings.this;
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (key.equals("password")) {
                            Intent intent = new Intent(context, NewPassword.class);
                            startActivity(intent);
                        }
                    }
                };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(),
                    R.xml.settings, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);
        }
    }


}