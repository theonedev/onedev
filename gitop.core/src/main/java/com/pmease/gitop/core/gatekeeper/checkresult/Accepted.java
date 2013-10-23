package com.pmease.gitop.core.gatekeeper.checkresult;

import java.util.List;

@SuppressWarnings("serial")
public class Accepted extends CheckResult {
    
    public Accepted(List<String> reasons) {
        super(reasons);
    }
    
    public Accepted(String reason) {
        super(reason);
    }

    @Override
    public String toString() {
        return "Accepted";
    }
    
}
