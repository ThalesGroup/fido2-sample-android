/*
 * Copyright © 2021-2022 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample

import androidx.fragment.app.FragmentActivity
import com.thalesgroup.gemalto.fido2.Fido2ErrorCode
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.authenticator.biometric.BiometricAuthenticatorCallback
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.client.Fido2Request
import com.thalesgroup.gemalto.fido2.client.Fido2RespondArgs
import com.thalesgroup.gemalto.fido2.client.Fido2Response
import com.thalesgroup.gemalto.fido2.client.Fido2ResponseCallback
import com.thalesgroup.gemalto.fido2.client.Fido2UiCallback
import com.thalesgroup.gemalto.fido2.sample.domain.logger.Logger
import com.thalesgroup.gemalto.fido2.sample.ui.fragment.HomeFragment.OnExecuteFinishListener
import com.thalesgroup.gemalto.fido2.sample.util.JsonUtil.prettyPrintJSON
import com.thalesgroup.gemalto.fido2.sample.util.LogUtil
import com.thalesgroup.gemalto.fido2.ui.SampleBiometricAuthenticatorCallback
import com.thalesgroup.gemalto.fido2.ui.SampleFido2UiCallback
import com.thalesgroup.gemalto.fido2.ui.SamplePasscodeLockoutUi
import com.thalesgroup.gemalto.fido2.ui.SamplePinPadAuthenticatorCallback

class Authenticate(private val activity: FragmentActivity, private val logger: Logger?) {
    fun execute(listener: OnExecuteFinishListener) {
        Thread(object : Runnable {
            override fun run() {
                // Fido2 authentication request json string
                val jsonString = "{\n" +
                        "          \"userVerification\" : \"required\",\n" +
                        "          \"challenge\" : \"AAABcqFVwBmaMa534c9FKrv7163Penj7\",\n" +
                        "          \"rpId\" : \"" + Configuration.rpId + "\"" +
                        "        }"

                // Log Registration request json string into Log view.
                logger?.log("Authentication Request:\n" + prettyPrintJSON(jsonString))

                try {
                    // Create Authentication request providing the required credentials.
                    /* 1 */
                    ## Create Fido2 request with json String ##

                    // Setup an instance of Fido2RespondArgsBuilder with Authentication request
                    // Initialize all necessary UI callbacks required by FIDO2 SDK.
                    // Ensure that you conform to these corresponding callbacks.
                    // Required callbacks are essential to ensure a proper UX behaviour.
                    // As a means of convenience, the FIDO2 UI SDK provides a wrapper class which conforms to all necessary callbacks of FIDO2 SDK
                    /* 2 */
                    ## Setup Fido2RespondArgs with UI callbacks ##

                    // Retrieve the FIDO2 Authentication response.
                    // Handle on error or response
                    /* 3 */
                    ## Retrieve FIDO2 Authentication response ##

                } catch (e: Fido2Exception) {
                    listener.onError(e)
                    LogUtil.instance.logMessage("Error", e)
                    e.printStackTrace()
                }
            }
        }).start()
    }
}
