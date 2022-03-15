package hu.elte.sbzbxr.phoneconnect.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import hu.elte.sbzbxr.phoneconnect.R;
import hu.elte.sbzbxr.phoneconnect.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.settings_fragment_container, new SettingsFragment())
                .commit();
    }

}