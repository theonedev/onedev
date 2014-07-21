package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

/* Reject the merge request. */
@SuppressWarnings("serial")
public class Disapproved extends CheckResult {

	public Disapproved(List<String> reasons) {
		super(reasons);
	}

	public Disapproved(String reason) {
		super(reason);
	}

    @Override
    public String toString() {
        return "Disapproved";
    }
    
}