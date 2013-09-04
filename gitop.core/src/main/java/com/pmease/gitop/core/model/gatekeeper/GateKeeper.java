package com.pmease.gitop.core.model.gatekeeper;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public interface GateKeeper extends Trimmable {
	
	public enum CheckResult {
		/* accept the merge request. */
		ACCEPT, 
		
		/* reject the merge request. */
		REJECT, 
		
		/* merge request acceptance check is pending and result is unknown yet */
		PENDING,  
		
		/* 
		 * same as PENDING, but followed gate keeper should not be checked unless result 
		 * of this gate keeper has been determined.
		 */
		PENDING_BLOCK 
	};
	
	CheckResult check(MergeRequest request);
}
