package com.thalesgroup.gemalto.fido2.sample.domain.logger;

import java.util.List;

public interface Logger {

    void log(String text);
    List<String> getLogs();
    void clean();


}
