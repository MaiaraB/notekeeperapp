package com.example.notekeeperapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowHomeEnabled(true);
        }


    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
//        public static final String PREF_DISPLAY_NAME = "user_display_name";
//        public static final String PREF_EMAIL_ADDRESS = "user_email_address";
//        public static final String PREF_FAVORITE_SOCIAL = "user_favorite_social";
//        private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener;
//
//        @Override
//        public void onCreate(@Nullable Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//
//            mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
//                @Override
//                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//                    if (key == PREF_DISPLAY_NAME) {
//                        Preference displayNamePref = findPreference(key);
//                        displayNamePref.setSummary(
//                                sharedPreferences.getString(key, getString(R.string.pref_default_display_name)));
//                    } else if (key == PREF_EMAIL_ADDRESS) {
//                        Preference emailAddressPref = findPreference(key);
//                        emailAddressPref.setSummary(
//                                sharedPreferences.getString(key, getString(R.string.pref_default_email_address)));
//                    } else if (key == PREF_FAVORITE_SOCIAL) {
//                        Preference favoriteSocialPref = findPreference(key);
//                        favoriteSocialPref.setSummary(
//                                sharedPreferences.getString(key, getString(R.string.pref_default_favorite_social)));
//                    }
//                }
//            };
//        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

//        @Override
//        public void onResume() {
//            super.onResume();
//
//            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
//        }
//
//        @Override
//        public void onPause() {
//            super.onPause();
//
//            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
//        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}