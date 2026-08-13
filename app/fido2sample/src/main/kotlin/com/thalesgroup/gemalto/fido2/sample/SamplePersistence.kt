/*
 * Copyright © 2026 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import android.content.Context
import androidx.core.content.edit

object SamplePersistence {
    private const val PREF_FILE_NAME = "fido2_sample_legal_prefs"
    private const val KEY_EULA_ACCEPTED = "IsEulaAcceptedKey"

    @JvmStatic
    fun isEulaAccepted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_EULA_ACCEPTED, false)
    }

    @JvmStatic
    fun setEulaAccepted(context: Context, isEulaAccepted: Boolean) {
        val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_EULA_ACCEPTED, isEulaAccepted) }
    }
}
