package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

/* Reject the merge request. */
@SuppressWarnings("serial")
public class Failed extends GateCheckResult {

	public Failed(List<String> reasons) {
		super(reasons);
	}

    @Override
    public String toString() {
        return "Failed";
    }
    
}