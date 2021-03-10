package com.thalesgroup.gemalto.fido2.ui;

import com.thalesgroup.gemalto.fido2.authenticator.biometric.BiometricAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.client.Fido2OperationInfo;

public class SampleBiometricAuthenticatorCallback implements BiometricAuthenticatorCallback {
    @Override
    public String biometricAuthenticatorCustomMessagePrompt(Fido2OperationInfo operationInfo) {
        return "Biometric verification for " + operationInfo.getRpId();
    }
}
