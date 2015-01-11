package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.ArrayList;


/**
 * Represents an ignored result, for instance if some check is not applicable, or if 
 * some user selected to ignore the vote. 
 * 
 * @author robin
 *
 */
@SuppressWarnings("serial")
public class Ignored extends CheckResult {
    
    public Ignored() {
        super(new ArrayList<String>());
    }

    @Override
    public String toString() {
        return "Ignored";
    }
    
}
