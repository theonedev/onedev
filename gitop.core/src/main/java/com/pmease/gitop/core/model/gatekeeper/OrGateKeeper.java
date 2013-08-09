package com.pmease.gitop.core.model.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import com.pmease.gitop.core.model.InvolvedCommit;

public class OrGateKeeper implements GateKeeper {

	private List<GateKeeper> gateKeepers = new ArrayList<GateKeeper>();
	
	public List<GateKeeper> getGateKeepers() {
		return gateKeepers;
	}

	public void setGateKeepers(List<GateKeeper> gateKeepers) {
		this.gateKeepers = gateKeepers;
	}

	@Override
	public CHECK_RESULT check(InvolvedCommit commit) {
		boolean undetermined = false;
		
		for (GateKeeper each: getGateKeepers()) {
			CHECK_RESULT result = each.check(commit);
			if (result == CHECK_RESULT.ACCEPT)
				return result;
			else if (result == CHECK_RESULT.UNDETERMINED)
				undetermined = true;
		}
		
		if (undetermined)
			return CHECK_RESULT.UNDETERMINED;
		else
			return CHECK_RESULT.REJECT;
	}

}
