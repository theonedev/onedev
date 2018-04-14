package io.onedev.server.util;

import java.io.Serializable;
import java.util.List;

import io.onedev.server.model.Issue;

public class MultiValueIssueField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Issue issue;
	
	private final String name;
	
	private final String type;
	
	private final List<String> values;
	
	public MultiValueIssueField(Issue issue, String name, String type, List<String> values) {
		this.issue = issue;
		this.name = name;
		this.type = type;
		this.values = values;
	}

	public Issue getIssue() {
		return issue;
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
