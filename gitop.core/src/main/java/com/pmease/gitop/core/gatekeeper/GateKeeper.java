package com.pmease.gitop.core.gatekeeper;

import com.pmease.commons.util.trimmable.Trimmable;
import com.pmease.gitop.core.model.MergeRequest;

public interface GateKeeper extends Trimmable {
	
	public enum CheckResult {
		/* accept the merge request. */
		ACCEPT {
			@Override
			public boolean isPending() {
				return false;
			}
		}, 
		
		/* reject the merge request. */
		REJECT {
			@Override
			public boolean isPending() {
				return false;
			}
		}, 
		
		/* merge request acceptance check is pending and result is unknown yet */
		PENDING {
			@Override
			public boolean isPending() {
				return true;
			}
		},  
		
		/* 
		 * same as PENDING, but followed gate keeper should not be checked unless result 
		 * of this gate keeper has been determined.
		 */
		PENDING_BLOCK {
			@Override
			public boolean isPending() {
				return true;
			}
		};
		
		public abstract boolean isPending();
		
	};
	
	CheckResult check(MergeRequest request);
}
