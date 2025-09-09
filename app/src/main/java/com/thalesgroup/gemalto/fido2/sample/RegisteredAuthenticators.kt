/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorRegistrationInfo
import com.thalesgroup.gemalto.fido2.client.Fido2Client
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory

class RegisteredAuthenticators(private val activity: FragmentActivity) {
    fun execute(): MutableList<Fido2AuthenticatorRegistrationInfo?> {
        var client: Fido2Client? = null
        try {
            client = Fido2ClientFactory.createFido2Client(activity.getApplicationContext())
            client.setActivity(activity)
        } catch (e: Fido2Exception) {
            showAlertDialog(
                activity.getString(R.string.error_alert_title),
                e.message ?: activity.getString(R.string.error_alert_title),
                false
            )
        }
        // To get the registered authenticators
        return client?.authenticatorRegistrations() ?: mutableListOf()
    }

    private fun showAlertDialog(title: String?, message: String?, popBack: Boolean = false) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    if (popBack) {
                        activity.supportFragmentManager.popBackStack()
                    }
                }
                .setCancelable(false)
                .show()
        }
    }


}
