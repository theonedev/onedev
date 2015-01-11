package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

/* 
 * Same as Pending, but followed gate keeper should not be checked unless result 
 * of this gate keeper has been determined.
 */
@SuppressWarnings("serial")
public class Blocking extends CheckResult {
	
	public Blocking(List<String> reasons) {
		super(reasons);
	}

	@Override
    public String toString() {
        return "Blocked";
    }
    
}