/*
 * Copyright © 2021-2022 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.thalesgroup.gemalto.fido2.client.Fido2Config
import com.thalesgroup.gemalto.fido2.client.VerifyMethod
import com.thalesgroup.gemalto.fido2.sample.AppUtils.getPinningCertificates
import com.thalesgroup.gemalto.fido2.sample.ui.fragment.HomeFragment.Companion.newInstance
import com.thalesgroup.gemalto.fido2.sample.ui.fragment.SettingsFragment
import com.thalesgroup.gemalto.securelog.SecureLogConfig

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var navigation: BottomNavigationView? = null
    private var eulaDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()

        //getting bottom navigation view and attaching the listener
        navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation?.setOnNavigationItemSelectedListener(this)

        //Initialize the secure log. It's mandatory configuration before using the FIDO2 API's.
        //Please refer the public key modulus & key component in the Configuration.java
        val secureLogConfig = SecureLogConfig.Builder(getApplicationContext())
            .publicKey(Configuration.publicKeyModulus, Configuration.publicKeyExponent)
            .fileID(getString(R.string.fido2_secure_log_file_id))
            .build()

        SecureLogArchive.mSecureLog = Fido2Config.setUpSecureLog(secureLogConfig)
        Fido2Config.setTlsCertificates(getPinningCertificates(this))

        // This is only called when the customer needs to use a custom AAGUID value.
        Configuration.customBiometricAaguid?.let { Fido2Config.setAuthenticatorAaguid(it, VerifyMethod.BIOMETRIC) }
        Configuration.customPasscodeAaguid?.let { Fido2Config.setAuthenticatorAaguid(it, VerifyMethod.PASSCODE) }

        //loading the default fragment
        loadFragment(newInstance())
    }

    override fun onResume() {
        super.onResume()
        if (!SamplePersistence.isEulaAccepted(this)) {
            showEulaPrompt()
        }
    }

    private fun showEulaPrompt() {
        if (eulaDialog?.isShowing == true) {
            return
        }
        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.fido2_sample_eula_title)
            .setMessage(R.string.fido2_sample_eula_message)
            .setCancelable(false)
            .setNegativeButton(R.string.fido2_sample_alert_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.fido2_sample_eula_proceed) { _, _ ->
                onTextClickedEndUserLicenseAgreement()
            }
        eulaDialog = builder.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null

        when (item.getItemId()) {
            R.id.navigation_home -> fragment = newInstance()
            R.id.navigation_authenticators -> fragment = SettingsFragment(this)
        }

        return loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            val fragmentManager = getSupportFragmentManager()
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, fragment)
            fragmentTransaction.commit()
            return true
        }
        return false
    }

    private fun onTextClickedEndUserLicenseAgreement() {
        SamplePersistence.setEulaAccepted(this, true)
        val intent = Intent(Intent.ACTION_VIEW, Configuration.CFG_EULA_URL)
        startActivity(intent)
    }
}
