package com.gitplex.server.gatekeeper.checkresult;

import java.util.List;

@SuppressWarnings("serial")
public class Passed extends GateCheckResult {
    
    public Passed(List<String> reasons) {
        super(reasons);
    }
    
    @Override
    public String toString() {
        return "Passed";
    }
    
}
