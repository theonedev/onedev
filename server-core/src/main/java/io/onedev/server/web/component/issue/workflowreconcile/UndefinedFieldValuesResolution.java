package io.onedev.server.web.component.issue.workflowreconcile;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class UndefinedFieldValuesResolution implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, String> renames;
	
	private final Collection<String> deletions;
	
	public UndefinedFieldValuesResolution(Map<String, String> renames, Collection<String> deletions) {
		this.renames = renames;
		this.deletions = deletions;
	}

	public Map<String, String> getRenames() {
		return renames;
	}

	public Collection<String> getDeletions() {
		return deletions;
	}
	
}
