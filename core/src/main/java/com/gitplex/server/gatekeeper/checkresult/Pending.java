package com.gitplex.server.gatekeeper.checkresult;

import java.util.List;

/* merge request acceptance check is pending and result is unknown yet */
@SuppressWarnings("serial")
public class Pending extends GateCheckResult {

	public Pending(List<String> reasons) {
		super(reasons);
	}

	@Override
    public String toString() {
        return "Pending";
    }
    
}