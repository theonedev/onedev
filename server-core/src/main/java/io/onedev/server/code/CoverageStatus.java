package io.onedev.server.code;

public enum CoverageStatus {
	
	COVERED, NOT_COVERED, PARTIALLY_COVERED;
	
	public CoverageStatus mergeWith(CoverageStatus status) {
		if (this == COVERED || status == COVERED)
			return COVERED;
		else if (this == NOT_COVERED && status == NOT_COVERED)
			return NOT_COVERED;
		else
			return PARTIALLY_COVERED;
	}
	
}
