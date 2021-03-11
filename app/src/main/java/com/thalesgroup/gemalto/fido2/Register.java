package com.thalesgroup.gemalto.fido2;

import androidx.fragment.app.FragmentActivity;

import com.thalesgroup.gemalto.fido2.sample.Configuration;
import com.thalesgroup.gemalto.fido2.sample.domain.logger.Logger;
import com.thalesgroup.gemalto.fido2.sample.util.JsonUtil;

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
                String jsonString ="{\n" +
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
                logger.log("Registration Request:\n" + JsonUtil.prettyPrintJSON(jsonString));


                // # 1: Create Registration request providing the required credentials. #

                // # 2: Setup Fido2RespondArgs with UI callbacks #

                // # 3: Retrieve the FIDO2 Registration response. #


            }
        }).start();

    }
}
