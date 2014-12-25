package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

/* 
 * Same as Pending, but followed gate keeper should not be checked unless result 
 * of this gate keeper has been determined.
 */
@SuppressWarnings("serial")
public class PendingAndBlock extends CheckResult {
	
	public PendingAndBlock(List<String> reasons) {
		super(reasons);
	}

	public PendingAndBlock(String reason) {
		super(reason);
	}

	@Override
    public String toString() {
        return "Blocked";
    }
    
}