package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import io.onedev.server.model.Issue;

public class PromptedField implements Serializable {

	private static final long serialVersionUID = 1L;

	@XStreamOmitField
	private final Issue issue;
	
	private final String name;
	
	@XStreamOmitField
	private final String type;
	
	private final List<String> values;
	
	public PromptedField(Issue issue, String name, String type, List<String> values) {
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
