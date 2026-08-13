/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorRegistrationInfo
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.sample.R
import com.thalesgroup.gemalto.fido2.sample.util.Base64
import java.text.SimpleDateFormat
import java.util.Locale

class AuthenticatorRecyclerViewAdapter(
    private val activity: FragmentActivity,
    private var registrationInfoList: MutableList<Fido2AuthenticatorRegistrationInfo>
) : RecyclerView.Adapter<AuthenticatorRecyclerViewAdapter.ViewHolder>() {

    private var isEditModeEnabled = false

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (viewType == 0)
            R.layout.item_authenticator_first
        else
            R.layout.item_authenticator_normal
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = registrationInfoList[position]
        val base64Flags = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

        val rpIdHash = Base64.encodeToString(info.rpIdHash, base64Flags)
        val credentialId = Base64.encodeToString(info.credentialId, base64Flags)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        holder.tvAuth.text = info.name
        holder.tvRp.text = rpIdHash
        holder.tvCred.text = credentialId
        holder.tvUserName.text = info.userName
        holder.tvUserDisplayName.text = info.userDisplayName
        holder.tvRpId.text = info.rpId
        holder.tvCreationDate.text = info.creationDate?.let { dateFormat.format(it) } ?: ""
        holder.tvLastTimeUsed.text = info.lastUsedDate?.let { dateFormat.format(it) } ?: ""

        // Show/hide delete button based on edit mode
        holder.btnDelete.visibility = if (isEditModeEnabled) View.VISIBLE else View.GONE

        holder.btnDelete.setOnClickListener {
            try {
                val client = Fido2ClientFactory.createFido2Client(activity.applicationContext)
                client.setActivity(activity)
                client.deleteAuthenticatorRegistration(info)
                updateRegistrationInfo()
            } catch (_: Fido2Exception) {}
        }
    }

    override fun getItemCount(): Int = registrationInfoList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAuth: TextView = view.findViewById(R.id.tv_auth)
        val tvRp: TextView = view.findViewById(R.id.tv_rp)
        val tvCred: TextView = view.findViewById(R.id.tv_cred)
        val tvUserName: TextView = view.findViewById(R.id.tv_user_name)
        val tvUserDisplayName: TextView = view.findViewById(R.id.tv_user_display_name)
        val tvRpId: TextView = view.findViewById(R.id.tv_rp_id)
        val tvCreationDate: TextView = view.findViewById(R.id.tv_creation_date)
        val tvLastTimeUsed: TextView = view.findViewById(R.id.tv_last_time_used)
        val btnDelete: Button = view.findViewById(R.id.btm_delete)
    }

    fun setEditModeOnOff(enabled: Boolean) {
        isEditModeEnabled = enabled
        notifyDataSetChanged()
    }

    fun updateRegistrationInfo() {
        val client = try {
            Fido2ClientFactory.createFido2Client(activity.applicationContext).apply { setActivity(activity) }
        } catch (_: Fido2Exception) { null }
        registrationInfoList = client?.authenticatorRegistrations()?.toMutableList() ?: mutableListOf()
        notifyDataSetChanged()
    }
}
