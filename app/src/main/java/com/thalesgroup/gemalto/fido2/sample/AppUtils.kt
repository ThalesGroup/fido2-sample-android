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
        val certificates = arrayOfNulls<X509Certificate>(1)


        /* Example on how to add more certs
       certificates[0] = AppUtils.getCertificate(context, R.raw.leaf_cert);
        certificates[1] = AppUtils.getCertificate(context, R.raw.intermediate_cert);*/
        certificates[0] = getCertificate(context, R.raw.intermediate_cert)

        return certificates
    }

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
