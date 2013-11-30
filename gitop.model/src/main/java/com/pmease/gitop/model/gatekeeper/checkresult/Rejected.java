package com.pmease.gitop.model.gatekeeper.checkresult;

import java.util.List;

/* Reject the merge request. */
@SuppressWarnings("serial")
public class Rejected extends CheckResult {

	public Rejected(List<String> reasons) {
		super(reasons);
	}

	public Rejected(String reason) {
		super(reason);
	}

    @Override
    public String toString() {
        return "Reject";
    }
    
}