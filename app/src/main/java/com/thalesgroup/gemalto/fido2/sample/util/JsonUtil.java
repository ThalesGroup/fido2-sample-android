package com.thalesgroup.gemalto.fido2.sample.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonUtil {

    /**
     * Attempt to format string into pretty-printed JSON.
     *
     * @param json
     * @return Pretty-printed json string if input is JSONObject or JSONArray. Else original string
     */
    public static String prettyPrintJSON(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            return arr.toString(4);
        } catch (JSONException e) {
            try {
                JSONObject obj = new JSONObject(json);
                return obj.toString(4);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return json;
    }
}
