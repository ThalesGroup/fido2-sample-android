/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.util

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonUtil {
    /**
     * Attempt to format string into pretty-printed JSON.
     *
     * @param json
     * @return Pretty-printed json string if input is JSONObject or JSONArray. Else original string
     */
    @JvmStatic
    fun prettyPrintJSON(json: String?): String? {
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
