/*
 * Copyright © 2021-2022 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.thalesgroup.gemalto.fido2.Fido2ErrorCode
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticator
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticatorCallback
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.client.Fido2Config
import com.thalesgroup.gemalto.fido2.sample.Configuration
import com.thalesgroup.gemalto.fido2.sample.R
import com.thalesgroup.gemalto.fido2.sample.SecureLogArchive
import com.thalesgroup.gemalto.fido2.sample.ui.fragment.AuthenticatorListFragment.Companion.newInstance
import com.thalesgroup.gemalto.fido2.ui.SamplePasscodeLockoutUi
import com.thalesgroup.gemalto.fido2.ui.SamplePinPadAuthenticatorCallback

class SettingsFragment(private val activity: FragmentActivity) : PreferenceFragmentCompat(),
    Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private var registeredAuthenticators: Preference? = null
    private var passcodeAuthenticator: PasscodeAuthenticator? = null
    private lateinit var passcodeAuthenticatorCallback: PasscodeAuthenticatorCallback
    private var createPasscode: Preference? = null
    private var changePasscode: Preference? = null
    private var deletePasscode: Preference? = null
    private var maxRetryCount: Preference? = null
    private var baseLockoutDuration: Preference? = null
    private var shareSecureLogs: Preference? = null
    private var reset: Preference? = null

    private var privacyPolicy: Preference? = null
    private var setPasscodeRule: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        // Show the registered authenticators
        registeredAuthenticators =
            findPreference(getString(R.string.fido2_sample_reg_authenticator_key))
        registeredAuthenticators?.setOnPreferenceClickListener(this)

        // Enroll the passcode authenticator
        createPasscode =
            findPreference(getString(R.string.fido2_sample_create_passcode_key))
        createPasscode?.setOnPreferenceClickListener(this)

        // Change the passcode
        changePasscode =
            findPreference(getString(R.string.fido2_sample_change_passcode_key))
        changePasscode?.setOnPreferenceClickListener(this)

        // Unenroll the passcode authenticator
        deletePasscode =
            findPreference(getString(R.string.fido2_sample_delete_passcode_key))
        deletePasscode?.setOnPreferenceClickListener(this)

        // Set the max retry count
        maxRetryCount = findPreference(getString(R.string.fido2_sample_max_retry_key))
        maxRetryCount?.setOnPreferenceChangeListener(this)

        // Set the base lockout duration
        baseLockoutDuration =
            findPreference(getString(R.string.fido2_sample_base_lockout_key))
        baseLockoutDuration?.setOnPreferenceChangeListener(this)

        // Set passcode rules preference
        setPasscodeRule = findPreference(getString(R.string.pref_key_passcode_rules_set))
        setPasscodeRule?.setOnPreferenceClickListener(this)

        // Share the secure logs by email to the thales fido team
        shareSecureLogs =
            findPreference(getString(R.string.fido2_sample_share_logs_key))
        shareSecureLogs?.setOnPreferenceClickListener(this)

        // Delete all the registered authenticators
        reset = findPreference(getString(R.string.fido2_sample_reset_key))
        reset?.setOnPreferenceClickListener(this)

        privacyPolicy = findPreference(getString(R.string.fido2_sample_privacy_policy_key))
        privacyPolicy?.setOnPreferenceClickListener(this)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        if (preference === maxRetryCount) {
            // To set the maximum retry count
            Fido2Config.setMaximumRetryCount((newValue as String?)?.toInt() ?: return true)
        } else if (preference === baseLockoutDuration) {
            // To set the base lockout duration
            Fido2Config.setBaseLockoutDuration((newValue as String?)?.toInt() ?: return true)
        }
        return true
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        when (preference) {
            registeredAuthenticators -> {
                val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragment_container, newInstance())
                fragmentTransaction.addToBackStack("registeredAuthenticators")
                fragmentTransaction.commit()
            }
            createPasscode ->
                try {
                passcodeAuthenticatorCallback = SamplePinPadAuthenticatorCallback(activity)
                passcodeAuthenticator =
                    PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback)
                    if (passcodeAuthenticator?.isPasscodeCreated == true) {
                        Toast.makeText(requireContext(), "Password has already been created.", Toast.LENGTH_SHORT).show()
                    } else {
                        passcodeAuthenticator?.createPasscode()
                    }
                } catch (e: Fido2Exception) {
                    showAlertDialogWithCancel(
                        requireActivity().getString(R.string.error_alert_title),
                        e.message ?: requireActivity().getString(R.string.error_alert_title)
                    )
            }
            changePasscode ->
                try {
                    passcodeAuthenticatorCallback = SamplePinPadAuthenticatorCallback(activity)
                    passcodeAuthenticator =
                        PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback)
                    if (passcodeAuthenticator?.isPasscodeCreated == false) {
                        Toast.makeText(requireContext(), "User is not enrolled.", Toast.LENGTH_SHORT).show()
                    } else {
                        passcodeAuthenticator?.changePasscode()
                    }
                } catch (e: Fido2Exception) {
                    showAlertDialogWithCancel(
                        requireActivity().getString(R.string.error_alert_title),
                        e.message ?: requireActivity().getString(R.string.error_alert_title)
                    )
                }
            deletePasscode ->
                try {
                    passcodeAuthenticatorCallback = SamplePinPadAuthenticatorCallback(activity)
                    passcodeAuthenticator =
                        PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback)
                    if (passcodeAuthenticator?.isPasscodeCreated == false) {
                        Toast.makeText(requireContext(), "User is not enrolled.", Toast.LENGTH_SHORT).show()
                    } else {
                        passcodeAuthenticator?.deletePasscode()
                    }
                } catch (e: Fido2Exception) {
                    showAlertDialogWithCancel(
                        requireActivity().getString(R.string.error_alert_title),
                        e.message ?: requireActivity().getString(R.string.error_alert_title)
                    )
            }

            shareSecureLogs -> {
                val zipFile = SecureLogArchive.createSecureLogZip(requireContext())

                if (Build.VERSION.SDK_INT >= 24) {
                    val builder = VmPolicy.Builder()
                    StrictMode.setVmPolicy(builder.build())
                }

                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, SecureLogArchive.getEmailTitle(requireContext()))
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.fido2_sample_secureLog_email_content))
                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(zipFile))
                }
                startActivity(
                    Intent.createChooser(
                        sendIntent,
                        getString(R.string.fido2_sample_securelog_chooser_title)
                    )
                )
            }
            reset -> {
                showAlertDialogWithCancel(
                    getString(R.string.alert_reset),
                    getString(R.string.reset_alert_message)
                )
            }
            setPasscodeRule -> {
                // Navigate to PinSettingsFragment
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PinSettingsFragment())
                    .addToBackStack("pinSettings")
                    .commit()
            }
            privacyPolicy -> {
                onTextClickedPrivacyPolicy()
            }
        }
        return true
    }

    private fun onTextClickedPrivacyPolicy() {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Configuration.CFG_PRIVACY_POLICY_URL
        )
        startActivity(browserIntent)
    }

    private fun refreshFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SettingsFragment(activity))
            .commitAllowingStateLoss()
    }

    private fun showAlertDialogWithCancel(title: String?, message: String?) {
        requireActivity().runOnUiThread {
            val alertDialog = AlertDialog.Builder(requireActivity())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(
                    R.string.fido2_sample_reset_title
                ) { dialog, _ ->
                    try {
                        val client = Fido2ClientFactory.createFido2Client(requireContext())
                        client.setActivity(activity)
                        client.reset()
                    } catch (e: Fido2Exception) {
                        showAlertDialogWithCancel(
                            activity.getString(R.string.error_alert_title),
                            e.message ?: activity.getString(R.string.error_alert_title)
                        )
                    }
                    dialog.dismiss()
                }
                .show()
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireActivity().resources.getColor(R.color.colorRed))
        }
    }
}
