package com.thalesgroup.gemalto.fido2.sample;

public class Configuration {

    // Refer the release documentation, how to generate the public key and place it below for publicKeyModulus & publicKeyExponent
    public static final byte[] publicKeyModulus = new byte[] {

    };

    public static final byte[] publicKeyExponent = new byte[] {

    };

    //SafetyNetAttestationKey. Get new key from here: https://developer.android.com/training/safetynet/attestation#obtain-api-key, and put below.
    public final static String safetyNetAttestationKey = "";

    //Place your relying party address here
    public final static String rpId = "";
}
