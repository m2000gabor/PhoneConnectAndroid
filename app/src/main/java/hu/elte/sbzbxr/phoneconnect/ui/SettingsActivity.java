package hu.elte.sbzbxr.phoneconnect.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import hu.elte.sbzbxr.phoneconnect.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}