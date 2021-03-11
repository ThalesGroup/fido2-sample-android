package com.thalesgroup.gemalto.fido2;


import androidx.fragment.app.FragmentActivity;

import com.thalesgroup.gemalto.fido2.sample.Configuration;
import com.thalesgroup.gemalto.fido2.sample.domain.logger.Logger;
import com.thalesgroup.gemalto.fido2.sample.util.JsonUtil;

public class Authenticate {
    private FragmentActivity activity;
    private Logger logger;
    public Authenticate(FragmentActivity activity, Logger logger) {
        this.activity = activity;
        this.logger = logger;
    }

    public void execute() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Fido2 authentication request json string
                String authReqMsg = "{\n" +
                        "          \"userVerification\" : \"required\",\n" +
                        "          \"challenge\" : \"AAABcqFVwBmaMa534c9FKrv7163Penj7\",\n" +
                        "          \"rpId\" : \""+ Configuration.rpId+"\"" +
                        "        }";

                // Log Registration request json string into Log view.
                logger.log("Authentication Request:\n" + JsonUtil.prettyPrintJSON(authReqMsg));

                // # 1: Create Authentication request providing the required credentials. #

                // # 2: Setup Fido2RespondArgs with UI callbacks #

                // # 3: Retrieve the FIDO2 Authentication response. #

            }
        }).start();

    }

}
