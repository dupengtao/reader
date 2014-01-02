package com.sismics.reader.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sismics.reader.R;
import com.sismics.reader.util.ApplicationUtil;
import com.sismics.reader.util.PreferenceUtil;

/**
 * Settings fragment.
 *
 * @author bgamard.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // This preference is only for tablet users
        PreferenceCategory category = (PreferenceCategory) findPreference("pref_general_category");
        if (!getResources().getBoolean(R.bool.narrow_articles_enabled)) {
            category.removePreference(findPreference("pref_narrowArticles"));
        }

        // Initialize summaries
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, PreferenceUtil.PREF_ARTICLES_FETCHED);
        onSharedPreferenceChanged(sharedPreferences, PreferenceUtil.PREF_DEFAULT_SUBSCRIPTION);
        onSharedPreferenceChanged(sharedPreferences, PreferenceUtil.PREF_FONT_SIZE);

        // Initialize static text preferences
        Preference versionPref = findPreference("pref_version");
        versionPref.setSummary(getString(R.string.version) + " " + ApplicationUtil.getVersionName(getActivity())
                + " | " + getString(R.string.build) + " " + ApplicationUtil.getVersionCode(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }
}
