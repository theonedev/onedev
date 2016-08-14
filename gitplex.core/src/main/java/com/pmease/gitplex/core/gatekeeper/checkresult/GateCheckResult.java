package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public abstract class GateCheckResult implements Serializable {
	
	private final List<String> reasons;

    public GateCheckResult(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getReasons() {
		return reasons;
	}
	
    public boolean isFailed() {
    	return this instanceof Failed;
    }
    
    public boolean isPassed() {
    	return this instanceof Passed;
    }
    
    public boolean isPending() {
    	return this instanceof Pending;
    }

    public boolean isBlocking() {
    	return this instanceof Blocking;
    }
    
    public boolean isIgnored() {
    	return this instanceof Ignored;
    }
    
    public boolean allowIntegration() {
    	return isPassed() || isIgnored();
    }
    
}