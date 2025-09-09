/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.ui.adapter

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorRegistrationInfo
import com.thalesgroup.gemalto.fido2.client.Fido2Client
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.client.VerifyMethod
import com.thalesgroup.gemalto.fido2.sample.R
import com.thalesgroup.gemalto.fido2.sample.util.Base64
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuthenticatorRecyclerViewAdapter(
    private val activity: FragmentActivity,
    private var registrationInfoList: MutableList<Fido2AuthenticatorRegistrationInfo>
) : RecyclerView.Adapter<AuthenticatorRecyclerViewAdapter.ViewHolder?>() {
    private var isEditModeEnable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_authenticator_list, parent, false)
        return this.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info: Fido2AuthenticatorRegistrationInfo = registrationInfoList.get(position)
        val rpIdHash = Base64.encodeToString(
            info.getRpIdHash(),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val credentialId = Base64.encodeToString(
            info.getCredentialId(),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )

        holder.authenticatorCredId.setText(credentialId)
        holder.authenticatorRpIdHash.setText(rpIdHash)
        when (info.getVerifyMethod()) {
            VerifyMethod.PASSCODE -> holder.imgAuthenticator.setImageResource(R.drawable.ic_dialpad)
            VerifyMethod.BIOMETRIC -> holder.imgAuthenticator.setImageResource(R.drawable.ic_fingerprint)
            VerifyMethod.PLATFORM -> holder.imgAuthenticator.setImageResource(R.drawable.ic_platform)
            VerifyMethod.PLATFORM_LOCAL -> holder.imgAuthenticator.setImageResource(R.drawable.ic_platform_local)
            else -> {}
        }

        holder.imgRemove.setVisibility(if (isEditModeEnable) View.GONE else View.VISIBLE)

        //Delete Authenticator
        holder.imgRemove.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                // Create a Fido2 Client
                var client: Fido2Client? = null
                try {
                    client = Fido2ClientFactory.createFido2Client(activity.getApplicationContext())
                    client.setActivity(activity)
                    // Delete the selected authenticator
                    client.deleteAuthenticatorRegistration(info)
                } catch (e: Fido2Exception) {
                    showAlertDialog(
                        activity.getString(R.string.error_alert_title),
                        e.message ?: activity.getString(R.string.error_alert_title),
                        false
                    )
                }


                updateRegistrationInfo()
                showAlertDialog(
                    activity.getString(R.string.removeauthenticator_alert_title),
                    activity.getString(R.string.removeauthenticator_alert_message),
                    false
                )
            }
        })

        val createdTimestamp = info.creationDate
        val lastUsedTimestamp = info.lastUsedDate

        holder.createdTextView.text = "Created: ${formatDate(createdTimestamp)}"
        holder.lastUsedTextView.text = "Last Used: ${formatDate(lastUsedTimestamp)}"

    }

    override fun getItemCount(): Int {
        return registrationInfoList.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(
        mView
    ) {
        val imgAuthenticator: ImageView
        val authenticatorCredId: TextView
        val authenticatorRpIdHash: TextView
        val imgRemove: ImageView
        val createdTextView: TextView
        val lastUsedTextView: TextView

        init {
            imgAuthenticator = mView.findViewById<View?>(R.id.img_authenticator_icon) as ImageView
            authenticatorCredId =
                mView.findViewById<View?>(R.id.txt_authenticator_credId) as TextView
            authenticatorRpIdHash =
                mView.findViewById<View?>(R.id.txt_authenticator_rpId) as TextView
            imgRemove = mView.findViewById<View?>(R.id.img_delete_icon) as ImageView
            createdTextView = mView.findViewById(R.id.txt_authenticator_created)
            lastUsedTextView = mView.findViewById(R.id.txt_authenticator_last_used)
        }
    }

    fun updateRegistrationInfo() {
        // Create a Fido2 Client
        var client: Fido2Client? = null
        try {
            client = Fido2ClientFactory.createFido2Client(activity.getApplicationContext())
            client.setActivity(activity)
        } catch (e: Fido2Exception) {
        }
        // Get the registered authenticators
        registrationInfoList = client?.authenticatorRegistrations() ?: mutableListOf()
        notifyDataSetChanged()
    }


    protected fun showAlertDialog(title: String?, message: String?, popBack: Boolean) {
        activity.runOnUiThread(object : Runnable {
            override fun run() {
                val alertDialogBuilder = AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(
                        android.R.string.ok,
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialogInterface: DialogInterface, i: Int) {
                                dialogInterface.dismiss()
                                if (popBack) {
                                    // Back to the Settings Fragment
                                    activity.getSupportFragmentManager().popBackStack()
                                }
                            }
                        })

                val alertDialog = alertDialogBuilder.create()
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
            }
        })
    }

    fun setEditModeOnOff(mode: Boolean) {
        isEditModeEnable = mode
        notifyDataSetChanged()
    }

    private fun formatDate(date: Date?): String {
        if (date == null) return "N/A"
        val formatter = SimpleDateFormat("dd MMM yyyy 'at' hh:mma", Locale.getDefault())
        return formatter.format(date)
    }
}
