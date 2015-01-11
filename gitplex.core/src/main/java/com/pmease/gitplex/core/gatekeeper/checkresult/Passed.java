package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

@SuppressWarnings("serial")
public class Passed extends CheckResult {
    
    public Passed(List<String> reasons) {
        super(reasons);
    }
    
    @Override
    public String toString() {
        return "Passed";
    }
    
}
