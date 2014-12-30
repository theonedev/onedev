package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public abstract class CheckResult implements Serializable {
	
	private final List<String> reasons;

    public CheckResult(List<String> reasons) {
        this.reasons = reasons;
    }

    public CheckResult(String reason) {
        this.reasons = Lists.newArrayList(reason);
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

    public boolean isPendingAndBlock() {
    	return this instanceof PendingAndBlock;
    }
    
    public boolean isIgnored() {
    	return this instanceof Ignored;
    }
    
    public boolean allowIntegration() {
    	return isPassed() || isIgnored();
    }
    
}