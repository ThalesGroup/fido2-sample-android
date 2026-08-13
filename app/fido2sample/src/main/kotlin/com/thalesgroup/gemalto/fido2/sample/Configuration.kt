/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import android.net.Uri
import androidx.core.net.toUri

object Configuration {
    @JvmField
    //Replace this byte array with your own public key modulus.
    val publicKeyModulus: ByteArray = byteArrayOf(

    )

    @JvmField
    //Replace this byte array with your own public key exponent.
    val publicKeyExponent: ByteArray = byteArrayOf(

    )

    // Fido2 rpId, Replace this value to your own rpId
    const val rpId: String = "genuflecto.github.io"

    // Custom AAGUID override values.
    // Leave as null to use the SDK defaults. Set to your own AAGUID string only if
    // your deployment requires a custom authenticator AAGUID.
    val customBiometricAaguid: String? = null
    val customPasscodeAaguid: String? = null

    // URL to privacy policy page shown to the end user.
    val CFG_PRIVACY_POLICY_URL: Uri
        get() = "https://docs-cybersec.thalesgroup.com/bundle/latest-idcloud-fido/page/docs/tnc/mobile/privacy-policy-idcloud-fido-sample.html".toUri()

    // URL to EULA page shown to the end user.
    val CFG_EULA_URL: Uri
        get() = "https://docs-cybersec.thalesgroup.com/bundle/latest-idcloud-fido/page/docs/tnc/mobile/eula-idcloud-fido-sample.html".toUri()
}
