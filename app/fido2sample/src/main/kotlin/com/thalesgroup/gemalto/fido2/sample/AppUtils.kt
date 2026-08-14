/*
 * Copyright © 2025 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import android.content.Context
import android.util.Log
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

object AppUtils {
    private val TAG: String = AppUtils::class.java.getSimpleName()

    @JvmStatic
    fun getPinningCertificates(context: Context): Array<X509Certificate?> {
        // Add your own backend's TLS pinning certificates here, e.g.:
        //   return arrayOf(getCertificate(context, R.raw.your_cert))
        // Place the certificate files under app/fido2sample/src/main/res/raw/.
        return emptyArray()
    }

    @Suppress("unused")
    private fun getCertificate(context: Context, resId: Int): X509Certificate? {
        var certificate: X509Certificate? = null
        var caInput: InputStream? = null

        try {
            val cf = CertificateFactory.getInstance("X.509")
            caInput = BufferedInputStream(context.getResources().openRawResource(resId))
            certificate = cf.generateCertificate(caInput) as X509Certificate?
            Log.i(TAG, "ca=" + (certificate)?.getSubjectDN())
        } catch (ex: CertificateException) {
            Log.e(TAG, ex.message.toString())
        } finally {
            if (caInput != null) {
                try {
                    caInput.close()
                } catch (ex: IOException) {
                    Log.e(TAG, ex.message.toString())
                }
            }
        }
        return certificate
    }
}
