package com.thalesgroup.gemalto.fido2;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.thalesgroup.gemalto.fido2.authenticator.biometric.BiometricAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticator;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.client.Fido2Client;
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory;
import com.thalesgroup.gemalto.fido2.client.Fido2Request;
import com.thalesgroup.gemalto.fido2.client.Fido2RespondArgs;
import com.thalesgroup.gemalto.fido2.client.Fido2Response;
import com.thalesgroup.gemalto.fido2.client.Fido2ResponseCallback;
import com.thalesgroup.gemalto.fido2.client.Fido2UiCallback;
import com.thalesgroup.gemalto.fido2.sample.Configuration;
import com.thalesgroup.gemalto.fido2.sample.R;
import com.thalesgroup.gemalto.fido2.sample.domain.logger.Logger;
import com.thalesgroup.gemalto.fido2.sample.util.JsonUtil;
import com.thalesgroup.gemalto.fido2.ui.SampleBiometricAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.ui.SampleFido2UiCallback;
import com.thalesgroup.gemalto.fido2.ui.SamplePasscodeAuthenticatorCallback;

import java.util.UUID;

public class Register {
    private FragmentActivity activity;
    private Logger logger;
    private String userName;

    public Register(FragmentActivity activity, Logger logger, String userName) {
        this.activity = activity;
        this.logger = logger;
        this.userName = userName;
    }

    public void execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Fido2 registration request json string
                String regReqBody ="{\n" +
                        "    \"authenticatorSelection\" : {\n" +
                        "        \"userVerification\" : \"required\",\n" +
                        "        \"authenticatorAttachment\" : \"platform\",\n" +
                        "        \"requireResidentKey\" : false\n" +
                        "    },\n" +
                        "    \"user\" : {\n" +
                        "        \"name\" : \""+userName+"\",\n" +
                        "        \"displayName\" : \""+userName+"\",\n" +
                        "        \"id\" : \""+ UUID.randomUUID().toString()+"\"\n" +
                        "    },\n" +
                        "    \"attestation\" : \"direct\",\n" +
                        "    \"challenge\" : \"AAABcqFVwBmaMa534c9FKrv7163Penj7\",\n" +
                        "    \"rp\" : {\n" +
                        "        \"id\" : \""+Configuration.rpId+"\",\n" +
                        "        \"name\" : \""+Configuration.rpId+"\"\n" +
                        "    },\n" +
                        "    \"pubKeyCredParams\" : [\n" +
                        "        {\n" +
                        "            \"type\" : \"public-key\",\n" +
                        "            \"alg\" : -7\n" +
                        "        }\n" +
                        "    ]\n" +
                        "}";

                // Log Registration request json string into Log view.
                logger.log("Registration Request:\n" + JsonUtil.prettyPrintJSON(regReqBody));

                // Create a Fido2 Client
                Fido2Client client = Fido2ClientFactory.createFido2Client(activity);

                // Create UI callback
                Fido2UiCallback uiCallback = new SampleFido2UiCallback(activity);

                // Create Passcode Authenticator
                PasscodeAuthenticatorCallback passcodeAuthenticatorCallback = new SamplePasscodeAuthenticatorCallback(activity);
                PasscodeAuthenticator passcodeAuthenticator = PasscodeAuthenticator.of(activity, passcodeAuthenticatorCallback);

                //Create Biometric Authenticator Callback
                BiometricAuthenticatorCallback biometricAuthenticatorCallback = new SampleBiometricAuthenticatorCallback();

                try {
                    // Configure Fido2Request
                    Fido2Request fido2Request = Fido2Request.jsonText(regReqBody);

                    // Setup an instance of Fido2RespondArgs.Builder,initialize all necessary UI callbacks required by FIDO2 SDK.
                    Fido2RespondArgs args = new Fido2RespondArgs.Builder()
                            .setFido2Request(fido2Request)
                            .setUiCallback(uiCallback)
                            .setPasscodeAuthenticatorCallback(passcodeAuthenticatorCallback)
                            .setPasscodeAuthenticator(passcodeAuthenticator)
                            .setBiometricAuthenticatorCallback(biometricAuthenticatorCallback)
                            .build();

                    // Fetch a FIDO2 response.
                    client.respondWithArgs(args, new Fido2ResponseCallback() {
                        @Override
                        public void onResponded(Fido2Response response) {
                            ((SamplePasscodeAuthenticatorCallback) passcodeAuthenticatorCallback).dismissPasscodeAuthenticatorDialog();
                            showAlertDialog(activity.getString(R.string.register_alert_title), activity.getString(R.string.register_alert_message));
                            logger.log("Registration Response:\n" + JsonUtil.prettyPrintJSON(response.raw()));
                        }

                        @Override
                        public void onError(Fido2Exception exception) {
                            ((SamplePasscodeAuthenticatorCallback) passcodeAuthenticatorCallback).dismissPasscodeAuthenticatorDialog();
                            // Recursively get the all exception message
                            String errorMessage = exception.getMessage();
                            Throwable ex = exception.getCause();
                            while (ex != null) {
                                errorMessage+= "\n" + ex.getMessage();
                                ex = ex.getCause();
                            }

                            showAlertDialog(activity.getString(R.string.error_alert_title), "Fido2 Error: " + errorMessage);
                            logger.log("Fido2 Error:\n" + errorMessage);
                        }
                    });
                } catch (Fido2Exception e) {
                    showAlertDialog(activity.getString(R.string.error_alert_title), "Invalid Request.\n" + e.getLocalizedMessage());
                    logger.log("Invalid Request:\n" + e.getLocalizedMessage());
                }
            }
        }).start();

    }

    private void showAlertDialog(String title, String message) {
        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show());
    }
}
