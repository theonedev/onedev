package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

@SuppressWarnings("serial")
public class Passed extends CheckResult {
    
    public Passed(List<String> reasons) {
        super(reasons);
    }
    
    public Passed(String reason) {
        super(reason);
    }

    @Override
    public String toString() {
        return "Passed";
    }
    
}
