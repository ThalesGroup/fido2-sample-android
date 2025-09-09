package com.thalesgroup.gemalto.fido2.sample.ui.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeConfig
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeRule
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeRuleLength
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeRulePalindrome
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeRuleSeries
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeRuleUniform
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.sample.R

class PinSettingsFragment() : PreferenceFragmentCompat(),
    Preference.OnPreferenceClickListener {
    private var setRulesPref: Preference? = null
    private var minLengthPref: Preference? = null
    private var maxLengthPref: Preference? = null
    private var keyboardScramble: Preference? = null
    private var uniformRulePref: Preference? = null
    private var seriesRulePref: Preference? = null
    private var palindromeRulePref: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pin_settings, rootKey)

        setRulesPref = findPreference(getString(R.string.pref_key_passcode_rules_set))
        minLengthPref = findPreference(getString(R.string.fido2_sample_passcode_min_length_key))
        maxLengthPref = findPreference(getString(R.string.fido2_sample_passcode_max_length_key))
        keyboardScramble = findPreference(getString(R.string.fido2_sample_disable_scrambler_key))
        uniformRulePref = findPreference(getString(R.string.pref_key_passcode_rules_uniform))
        seriesRulePref = findPreference(getString(R.string.pref_key_passcode_rules_series))
        palindromeRulePref = findPreference(getString(R.string.pref_key_passcode_rules_palindrome))

        setRulesPref?.setOnPreferenceClickListener(null)

        minLengthPref?.setOnPreferenceChangeListener { _, newValue ->
            updatePasscodeRules(newMinLength = newValue as String)
            true
        }
        maxLengthPref?.setOnPreferenceChangeListener { _, newValue ->
            updatePasscodeRules(newMaxLength = newValue as String)
            true
        }
        keyboardScramble?.setOnPreferenceChangeListener { _, newValue ->
            updatePasscodeRules(newScrambleDisabled = newValue as Boolean)
            true
        }
        uniformRulePref?.setOnPreferenceChangeListener { _, newValue ->
            updatePasscodeRules(newUniformEnabled = newValue as Boolean)
            true
        }
        seriesRulePref?.setOnPreferenceChangeListener { _, newValue ->
            updatePasscodeRules(newSeriesEnabled = newValue as Boolean)
            true
        }
        palindromeRulePref?.setOnPreferenceChangeListener { _, newValue ->
            updatePasscodeRules(newPalindromeEnabled = newValue as Boolean)
            true
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference == setRulesPref) {
            updatePasscodeRules()
            return true
        }
        return false
    }

    private fun updatePasscodeRules(
        newMinLength: String? = null,
        newMaxLength: String? = null,
        newScrambleDisabled: Boolean? = null,
        newUniformEnabled: Boolean? = null,
        newSeriesEnabled: Boolean? = null,
        newPalindromeEnabled: Boolean? = null
    ) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val minLengthStr = newMinLength ?: prefs.getString(getString(R.string.fido2_sample_passcode_min_length_key), "")
        val maxLengthStr = newMaxLength ?: prefs.getString(getString(R.string.fido2_sample_passcode_max_length_key), "")
        val scrambleDisabled = newScrambleDisabled ?: prefs.getBoolean(getString(R.string.fido2_sample_disable_scrambler_key), false)
        val uniformEnabled = newUniformEnabled ?: prefs.getBoolean(getString(R.string.pref_key_passcode_rules_uniform), true)
        val seriesEnabled = newSeriesEnabled ?: prefs.getBoolean(getString(R.string.pref_key_passcode_rules_series), true)
        val palindromeEnabled = newPalindromeEnabled ?: prefs.getBoolean(getString(R.string.pref_key_passcode_rules_palindrome), true)

        val rules = mutableListOf<PasscodeRule>()

        val passcodeLengthRule = PasscodeRuleLength()
        if (!minLengthStr.isNullOrEmpty()) {
            passcodeLengthRule.setMinimumLength(minLengthStr.toInt())
        }
        if (!maxLengthStr.isNullOrEmpty()) {
            passcodeLengthRule.setMaximumLength(maxLengthStr.toInt())
        }
        rules.add(passcodeLengthRule)

        if (uniformEnabled) rules.add(PasscodeRuleUniform())
        if (seriesEnabled) rules.add(PasscodeRuleSeries())
        if (palindromeEnabled) rules.add(PasscodeRulePalindrome())

        if (scrambleDisabled) {
            PasscodeConfig.disableScramble()
        }

        try {
            PasscodeConfig.setPasscodeRules(rules.toTypedArray())
            Toast.makeText(requireContext(), getString(R.string.passcode_rules_set_success), Toast.LENGTH_SHORT).show()
        } catch (e: Fido2Exception) {
            showAlertDialogWithCancel(
                requireActivity().getString(R.string.error_alert_title),
                e.message ?: requireActivity().getString(R.string.error_alert_title)
            )
        }
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
                            activity?.getString(R.string.error_alert_title),
                            e.message ?: activity?.getString(R.string.error_alert_title)
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
