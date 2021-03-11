package com.thalesgroup.gemalto.fido2.sample.domain.logger;

import java.util.ArrayList;
import java.util.List;

public class LoggerImpl implements Logger {

    private List<String> items;

    public LoggerImpl() {
        items = new ArrayList<>();
    }

    @Override
    public void log(String text) {
        items.add(text);
    }

    @Override
    public List<String> getLogs() {
        return items;
    }

    @Override
    public void clean() {
        items.clear();
    }


}
