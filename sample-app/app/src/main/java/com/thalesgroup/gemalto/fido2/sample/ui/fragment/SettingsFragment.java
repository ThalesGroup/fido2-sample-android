package com.thalesgroup.gemalto.fido2.sample.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticator;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeConfig;
import com.thalesgroup.gemalto.fido2.client.Fido2Client;
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory;
import com.thalesgroup.gemalto.fido2.client.Fido2Config;
import com.thalesgroup.gemalto.fido2.sample.R;
import com.thalesgroup.gemalto.fido2.sample.SecureLogArchive;
import com.thalesgroup.gemalto.fido2.ui.SamplePasscodeAuthenticatorCallback;

import java.io.File;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private FragmentActivity activity;
    private Preference registeredAuthenticators;
    private PasscodeAuthenticator passcodeAuthenticator;
    private PasscodeAuthenticatorCallback passcodeAuthenticatorCallback;
    private Preference createPasscode;
    private Preference changePasscode;
    private Preference deletePasscode;
    private Preference keyboardScramble;
    private Preference minimumPasscodeLength;
    private Preference maximumPasscodeLength;
    private Preference maxRetryCount;
    private Preference baseLockoutDuration;
    private Preference shareSecureLogs;
    private Preference reset;

    private boolean scrambleStatus;

    public SettingsFragment (FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        // Show the registered authenticators
        registeredAuthenticators = findPreference(getString(R.string.fido2_sample_reg_authenticator_key));
        registeredAuthenticators.setOnPreferenceClickListener(this);

        // Enroll the passcode authenticator
        createPasscode = findPreference(getString(R.string.fido2_sample_create_passcode_key));
        createPasscode.setOnPreferenceClickListener(this);

        // Change the passcode
        changePasscode = findPreference(getString(R.string.fido2_sample_change_passcode_key));
        changePasscode.setOnPreferenceClickListener(this);

        // Unenroll the passcode authenticator
        deletePasscode = findPreference(getString(R.string.fido2_sample_delete_passcode_key));
        deletePasscode.setOnPreferenceClickListener(this);

        // Disable the keyboard scrambler
        keyboardScramble = findPreference(getString(R.string.fido2_sample_disable_scrambler_key));
        scrambleStatus = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getString(R.string.fido2_sample_disable_scrambler_key), false);
        if(scrambleStatus) {
            keyboardScramble.setEnabled(true);
            keyboardScramble.setSelectable(false);
            PasscodeConfig.disableScramble();
        }
        keyboardScramble.setOnPreferenceClickListener(this);

        // Set the minimum passcode length
        minimumPasscodeLength = findPreference(getString(R.string.fido2_sample_passcode_min_length_key));
        minimumPasscodeLength.setOnPreferenceChangeListener(this);

        // Set the maximum passcode length
        maximumPasscodeLength = findPreference(getString(R.string.fido2_sample_passcode_max_length_key));
        maximumPasscodeLength.setOnPreferenceChangeListener(this);

        // Set the max retry count
        maxRetryCount = findPreference(getString(R.string.fido2_sample_max_retry_key));
        maxRetryCount.setOnPreferenceChangeListener(this);

        // Set the base lockout duration
        baseLockoutDuration = findPreference(getString(R.string.fido2_sample_base_lockout_key));
        baseLockoutDuration.setOnPreferenceChangeListener(this);

        // Share the secure logs by email to the thales fido team
        shareSecureLogs = findPreference(getString(R.string.fido2_sample_share_logs_key));
        shareSecureLogs.setOnPreferenceClickListener(this);

        // Delete all the registered authenticators
        reset = findPreference(getString(R.string.fido2_sample_reset_key));
        reset.setOnPreferenceClickListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == minimumPasscodeLength) {
            // To set the minimum passcode length
            PasscodeConfig.setMinimumPasscodeLength(Integer.parseInt((String)newValue));
        } else if(preference == maximumPasscodeLength) {
            // To set the maximum passcode length
            PasscodeConfig.setMaximumPasscodeLength(Integer.parseInt((String)newValue));
        } else if(preference == maxRetryCount) {
            // To set the maximum retry count
            Fido2Config.setMaximumRetryCount(Integer.parseInt((String)newValue));
        } else if(preference == baseLockoutDuration) {
            // To set the base lockout duration
            Fido2Config.setBaseLockoutDuration(Integer.parseInt((String)newValue));
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference == registeredAuthenticators) {
            // Show the Authenticator List
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, AuthenticatorListFragment.newInstance());
            fragmentTransaction.addToBackStack("registeredAuthenticators");
            fragmentTransaction.commit();
        } else if(preference == createPasscode) {
            passcodeAuthenticatorCallback = new SamplePasscodeAuthenticatorCallback(activity);
            passcodeAuthenticator = PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback);
            // To enroll the passcode authenticator
            passcodeAuthenticator.createPasscode();
        } else if(preference == changePasscode) {
            passcodeAuthenticatorCallback = new SamplePasscodeAuthenticatorCallback(activity);
            passcodeAuthenticator = PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback);
            // To change the passcode authenticator
            passcodeAuthenticator.changePasscode();
        } else if(preference == deletePasscode) {
            passcodeAuthenticatorCallback = new SamplePasscodeAuthenticatorCallback(activity);
            passcodeAuthenticator = PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback);
            // To unenroll the passcode authenticator
            passcodeAuthenticator.deletePasscode();
        } else if(preference == keyboardScramble) {
            keyboardScramble.setEnabled(true);
            keyboardScramble.setSelectable(false);
            // To disable the keyboard scramble
            PasscodeConfig.disableScramble();
        } else if(preference == shareSecureLogs) {
            // Prepare secure log zip folder.
            File zipFile = SecureLogArchive.createSecureLogZip(getContext());

            //Sending secureLog through zip
            if (Build.VERSION.SDK_INT >= 24) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, SecureLogArchive.getEmailTitle(getContext()));
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.fido2_sample_secureLog_email_content));
            sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zipFile));
            startActivity(Intent.createChooser(sendIntent, getString(R.string.fido2_sample_securelog_chooser_title)));
        } else if(preference == reset) {
            showAlertDialogWithCancel(getString(R.string.alert_reset), getString(R.string.reset_alert_message));
        }
        return true;
    }

    private void showAlertDialogWithCancel(String title, String message) {
        getActivity().runOnUiThread(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(R.string.fido2_sample_reset_title, (dialog, which) -> {
                        Fido2Client client = Fido2ClientFactory.createFido2Client(getActivity());
                        // To delete all the registered authenticators
                        client.reset();
                        dialog.dismiss();
                    })
                    .show();
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getActivity().getResources().getColor(R.color.colorRed));
        });
    }
}