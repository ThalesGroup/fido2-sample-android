/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.thalesgroup.gemalto.securelog.SecureLog
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SecureLogArchive {
    var mSecureLog: SecureLog? = null

    fun createSecureLogZip(context: Context): File? {
        if (mSecureLog == null) {
            Toast.makeText(context, "SecureLog is not configure!", Toast.LENGTH_LONG)
                .show()
            return null
        }
        val slFiles = mSecureLog?.getFiles()
        if (slFiles == null || slFiles.isEmpty()) {
            Toast.makeText(context, "Log file list is empty!", Toast.LENGTH_LONG)
                .show()
            return null
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd(hh.mm)", Locale.US)
        val zFileName = "secureLog-" + sdf.format(Date()) + ".zip"
        val zipFile = File(context.getExternalFilesDir(null), zFileName)
        if (zipFile.exists()) {
            zipFile.delete()
        }

        try {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                val buffer = ByteArray(6 * 1024)
                var read: Int
                for (f in slFiles) {
                    FileInputStream(f).use { fis ->
                        val zipEntry = ZipEntry(f.getName())
                        zos.putNextEntry(zipEntry)
                        while ((fis.read(buffer).also { read = it }) != -1) zos.write(
                            buffer,
                            0,
                            read
                        )
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()

            val dlg = AlertDialog.Builder(context)
                .setTitle("Failed creating zip file")
                .setMessage("Can not create secureLog zip file, error: " + ex.message)
                .create()
            dlg.show()
        }

        return zipFile
    }

    fun getEmailTitle(context: Context): String {
        var title = ""
        try {
            val pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
            title += " - " + pInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        title += " is failing!"
        return title
    }
}


