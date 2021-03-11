package com.thalesgroup.gemalto.fido2.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thalesgroup.gemalto.fido2.client.Fido2Config;
import com.thalesgroup.gemalto.fido2.rasp.Rasp;
import com.thalesgroup.gemalto.fido2.sample.ui.fragment.HomeFragment;
import com.thalesgroup.gemalto.fido2.sample.ui.fragment.SettingsFragment;
import com.thalesgroup.gemalto.securelog.SecureLogConfig;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();

        //Setup the RASP Config
        Rasp.configure(Rasp.TYPE_DEBUGGER
                        | Rasp.TYPE_ROOT
                        | Rasp.TYPE_HOOK
                        | Rasp.TYPE_VIRTUAL_ENVIRONMENT_DETECTION
                        | Rasp.TYPE_TAMPER
                        | Rasp.TYPE_EMULATOR
                , Rasp.MODE_CRASH);

        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        //Initialize the secure log. It's mandatory configuration before using the FIDO2 API's.
        //Please refer the public key modulus & key component in the Configuration.java
        SecureLogConfig secureLogConfig = new SecureLogConfig.Builder(getApplicationContext())
                .publicKey(Configuration.publicKeyModulus, Configuration.publicKeyExponent)
                .fileID(getString(R.string.fido2_secure_log_file_id))
                .build();

        SecureLogArchive.mSecureLog = Fido2Config.setUpSecureLog(secureLogConfig);

        //Set safetyNet Attestation Key. Please refer the Configuration.java
        Fido2Config.setAttestationKey(Configuration.safetyNetAttestationKey);

        //loading the default fragment
        loadFragment(HomeFragment.newInstance());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = HomeFragment.newInstance();
                break;

            case R.id.navigation_authenticators:
                fragment = new SettingsFragment(this);
                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            return true;
        }
        return false;
    }
}
