package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

/* 
 * Same as Pending, but followed gatekeeper should not be checked unless result 
 * of this gatekeeper has been determined.
 */
@SuppressWarnings("serial")
public class Blocking extends GateCheckResult {
	
	public Blocking(List<String> reasons) {
		super(reasons);
	}

	@Override
    public String toString() {
        return "Blocked";
    }
    
}