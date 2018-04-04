package io.onedev.server.util;

import java.io.Serializable;
import java.util.List;

public class MultiValueIssueField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String type;
	
	private final List<String> values;
	
	public MultiValueIssueField(String name, String type, List<String> values) {
		this.name = name;
		this.type = type;
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<String> getValues() {
		return values;
	}
	
}
