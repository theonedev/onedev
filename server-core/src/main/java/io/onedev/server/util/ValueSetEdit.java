package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class ValueSetEdit implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Map<String, String> renames;
	
	private final Collection<String> deletions;
	
	public ValueSetEdit(Map<String, String> renames, Collection<String> deletions) {
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
