/*
 * Copyright © 2020 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.util

import android.os.Environment
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date

class LogUtil private constructor() {
    fun logMessage(tag: String, exception: Exception): Boolean {
        val trace = StringWriter()
        exception.printStackTrace(PrintWriter(trace))
        return logMessage(tag, exception.message + "\n" + trace.toString())
    }

    fun logMessage(tag: String, message: String?): Boolean {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(LOG_FILE, true)
            val prefix = "[" + tag + "] " + sDateFormatter.format(Date())
            val s = prefix + DATE_SEPARATOR + message + "\n"
            fos.write(s.toByteArray(Charset.forName("UTF-8")))
            return true
        } catch (e: IOException) {
            TestUtil.log(e)
            return false
        } finally {
            try {
                if (fos != null) {
                    fos.close()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun clearLog() {
        LOG_FILE.delete()
    }

    val logItems: MutableList<String?>
        get() {
            var reader: BufferedReader? = null
            val result: MutableList<String?> =
                ArrayList<String?>()
            try {
                reader =
                    BufferedReader(FileReader(LOG_FILE))

                var line = reader.readLine()
                var builder = StringBuilder()
                while (line != null) {
                    val dateIndex: Int =
                        line.indexOf(DATE_SEPARATOR)
                    var tagAndDate = ""
                    if (dateIndex > 0) {
                        if (builder.length != 0) {
                            result.add(builder.toString())
                            builder = StringBuilder()
                        }
                        tagAndDate = line.substring(0, dateIndex)
                    }
                    val content = line.substring(dateIndex + 1)
                    builder.append(tagAndDate)
                    builder.append("\n")

                    try {
                        //JSONArray jsonArray;
                        //if(checkIfJsonObject(content)){
                        //jsonArray = getJSONArray(content);
                        //} else {
                        //jsonArray = new JSONArray(content);
                        //}

                        // assuming it is fido request
                        //jsonArray = addAssertionInfo(jsonArray);

                        val jsonObject = JSONObject(content)
                        builder.append(jsonObject.toString())
                        //builder.append(jsonArray.toString(4));
                    } catch (e: JSONException) {
                        // not a json message
                        builder.append(content)
                    }

                    line = reader.readLine()
                }
                result.add(builder.toString())
                return result
            } catch (e: Exception) {
                TestUtil.log(e)
                return result
            } finally {
                try {
                    if (reader != null) reader.close()
                } catch (e: Exception) {
                }
            }
        }

    val logs: String
        get() {
            val builder = StringBuilder()
            var reader: BufferedReader? = null
            try {
                reader =
                    BufferedReader(FileReader(LOG_FILE))

                var line = reader.readLine()
                while (line != null) {
                    val dateIndex: Int =
                        line.indexOf(DATE_SEPARATOR)
                    var tagAndDate = ""
                    if (dateIndex > 0) {
                        tagAndDate = line.substring(0, dateIndex)
                    }
                    val content = line.substring(dateIndex + 1)
                    builder.append(tagAndDate)
                    builder.append("\n")

                    try {
                        val jsonArray: JSONArray?
                        if (checkIfJsonObject(content)) {
                            jsonArray = getJSONArray(content)
                        } else {
                            jsonArray = JSONArray(content)
                        }

                        // assuming it is fido request
                        //jsonArray = addAssertionInfo(jsonArray);
                        builder.append(jsonArray?.toString(4))
                    } catch (e: JSONException) {
                        // not a json message
                        builder.append(content)
                    }

                    builder.append("\n\n")
                    line = reader.readLine()
                }

                return builder.toString()
            } catch (e: Exception) {
                TestUtil.log(e)
                return builder.toString()
            } finally {
                try {
                    if (reader != null) reader.close()
                } catch (e: Exception) {
                }
            }
        }

    // Parse the JSON content and return the JSONArray
    private fun getJSONArray(content: String): JSONArray? {
        try {
            val jsonObject = JSONObject(content)
            return JSONArray(jsonObject.getString("uafProtocolMessage"))
        } catch (e: JSONException) {
            return null
        }
    }

    // Check the string content is the instance of JSON Object
    private fun checkIfJsonObject(content: String?): Boolean {
        try {
            val json = JSONTokener(content).nextValue()
            if (json is JSONObject) {
                return true
            }
        } catch (e: JSONException) {
            return false
        }
        return false
    }

    companion object {
        private val LOG_FILE = File(
            Environment.getExternalStorageDirectory(),
            "fido2test.log"
        )
        val instance: LogUtil = LogUtil()
        private const val DATE_SEPARATOR = "\t"
        private val sDateFormatter = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss")
        private const val TAG = "LogUtil"
    }
}
