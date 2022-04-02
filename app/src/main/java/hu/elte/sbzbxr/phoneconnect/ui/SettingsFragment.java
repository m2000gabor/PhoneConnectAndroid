package hu.elte.sbzbxr.phoneconnect.ui;

import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.controller.ServiceController;
import hu.elte.sbzbxr.phoneconnect.ui.notifications.NotificationSettings;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_xml, rootKey);


        Preference notificationPref = getPreferenceManager().findPreference("notification");
        if(notificationPref!=null) notificationPref.setOnPreferenceClickListener(
                preference -> {
                    System.out.println("preference clicked");
                    return false;
                });

        androidx.preference.EditTextPreference editTextPreference = getPreferenceManager().findPreference("network_speed");
        if(editTextPreference!=null) {
            editTextPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
        }
    }
}
