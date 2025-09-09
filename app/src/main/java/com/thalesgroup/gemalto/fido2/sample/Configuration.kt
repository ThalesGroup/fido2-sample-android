/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

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
}
