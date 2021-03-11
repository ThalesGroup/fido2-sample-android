package com.thalesgroup.gemalto.fido2;

import androidx.fragment.app.FragmentActivity;

import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorRegistrationInfo;
import com.thalesgroup.gemalto.fido2.client.Fido2Client;
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory;

import java.util.List;

public class RegisteredAuthenticators {
    private FragmentActivity activity;
    public RegisteredAuthenticators(FragmentActivity activity) {
        this.activity = activity;

    }

    public List<Fido2AuthenticatorRegistrationInfo> execute() {
        Fido2Client client = Fido2ClientFactory.createFido2Client(activity);
        // To get the registered authenticators
        return client.authenticatorRegistrations();
    }

}
