package com.pmease.gitop.core.hookcallback;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class Output {

    /*
     * Use this special character to indicate line breaker as normal 
     * line break cause the output difficult to parse in hook shell 
     * script.
     */
    private static final String LINE_BREAKER = "|";
    
    private List<String> lines = new ArrayList<String>();
    
    protected void writeLine(String line) {
        Preconditions.checkArgument(!line.contains(LINE_BREAKER), 
                "line should not contain character '" + LINE_BREAKER + "'.");
        lines.add(line);
    }
    
    protected void markError() {
        lines.add("ERROR");
    }

    @Override
    public String toString() {
        return StringUtils.join(lines, LINE_BREAKER);
    }
    
}
