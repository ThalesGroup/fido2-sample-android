/*
 * Copyright © 2020 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object TestUtil {
    fun log(msg: String) {
        Log.d("Fido2Demo", msg)
    }

    fun log(error: Exception) {
        Log.d("Fido2Demo", error.message, error)
    }

    fun hideKeyBoard(activity: Activity) {
        try {
            val view = activity.getCurrentFocus()
            if (view != null) {
                val imm =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            }
        } catch (e: Exception) {
            log(e)
        }
    }

    /**
     * Attempt to format string into pretty-printed JSON.
     *
     * @param json
     * @return Pretty-printed json string if input is JSONObject or JSONArray. Else original string
     */
    fun prettyPrintJSON(json: String): String? {
        try {
            val arr = JSONArray(json)
            return arr.toString(4)
        } catch (e: JSONException) {
            try {
                val obj = JSONObject(json)
                return obj.toString(4)
            } catch (e1: JSONException) {
                e1.printStackTrace()
            }
        }
        return json
    }
}
