package hu.elte.sbzbxr.phoneconnect.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import hu.elte.sbzbxr.phoneconnect.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_xml, rootKey);
    }
}
