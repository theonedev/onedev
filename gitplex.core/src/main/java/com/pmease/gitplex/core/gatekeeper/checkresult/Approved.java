package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

@SuppressWarnings("serial")
public class Approved extends CheckResult {
    
    public Approved(List<String> reasons) {
        super(reasons);
    }
    
    public Approved(String reason) {
        super(reason);
    }

    @Override
    public String toString() {
        return "Approved";
    }
    
}
