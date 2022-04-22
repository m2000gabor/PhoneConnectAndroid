package hu.elte.sbzbxr.phoneconnect.ui;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.model.persistance.MyPreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final int MAX_PORT_NUMBER = 65536;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_xml, rootKey);

        Preference notificationPref = getPreferenceManager().findPreference(getString(R.string.notification));
        if(notificationPref!=null) notificationPref.setOnPreferenceClickListener(
                preference -> {
                    System.out.println("preference clicked");
                    return false;
                });

        EditTextPreference networkSpeedPref = getPreferenceManager().findPreference(getString(R.string.network_speed));
        if(networkSpeedPref!=null) {
            networkSpeedPref.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
            networkSpeedPref.setSummary(MyPreferenceManager.getNetworkSpeedLimit(getContext()).toString());
            networkSpeedPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    preference.setSummary(newValue.toString() + " KB/s");
                    return true;
                }
            });
        }

        EditTextPreference portPreference = getPreferenceManager().findPreference(getString(R.string.port));
        if(portPreference!=null){
            portPreference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED));
            portPreference.setSummary(MyPreferenceManager.getPort(getContext()));
            portPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    if(newValue==null) return false;
                    boolean isValid = false;
                    try {
                        isValid = Integer.parseInt(newValue.toString())<MAX_PORT_NUMBER && Integer.parseInt(newValue.toString())>0;
                    }catch (NumberFormatException ignored){}

                    if(isValid){
                        preference.setSummary(newValue.toString());
                    }else{
                        Toast.makeText(getContext(),"The port must be a valid number between 0 and "+MAX_PORT_NUMBER,Toast.LENGTH_SHORT).show();
                    }
                    return isValid;
                }
            });
        }

        Preference ipAddressPreference = getPreferenceManager().findPreference(getString(R.string.ip_address));
        if(ipAddressPreference!=null){
            ipAddressPreference.setSummary(MyPreferenceManager.getAddress(getContext()));
            ipAddressPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                    preference.setSummary(newValue.toString());
                    return true;
                }
            });
        }

    }


}
