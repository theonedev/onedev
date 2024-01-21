package io.onedev.server.codequality;

import javax.annotation.Nullable;

public enum CoverageStatus {
	
	COVERED, NOT_COVERED, PARTIALLY_COVERED;
	
	public CoverageStatus mergeWith(@Nullable CoverageStatus status) {
		if (status != null) {
			if (this == COVERED || status == COVERED)
				return COVERED;
			else if (this == NOT_COVERED && status == NOT_COVERED)
				return NOT_COVERED;
			else
				return PARTIALLY_COVERED;
		} else {
			return this;
		}
	}
	
}
