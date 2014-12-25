package com.pmease.gitplex.core.gatekeeper.checkresult;

import java.util.List;

/* merge request acceptance check is pending and result is unknown yet */
@SuppressWarnings("serial")
public class Pending extends CheckResult {

	public Pending(List<String> reasons) {
		super(reasons);
	}

	public Pending(String reason) {
		super(reason);
	}

	@Override
    public String toString() {
        return "Pending";
    }
    
}