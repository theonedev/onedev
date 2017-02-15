package com.gitplex.server.util.execution;

import com.gitplex.server.util.StringUtils;

public class Argument {

    private String[] values;

    public void setValue(String value) {
        values = new String[] {value};
    }
    
    public void setLine(String line) {
        values = StringUtils.parseQuoteTokens(line);
    }
    
    public String[] getParts() {
        return values;
    }

}