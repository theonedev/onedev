package io.onedev.server.model.support.issue;

import java.io.Serializable;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class PromptedField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	@XStreamOmitField
	private final String type;
	
	private final List<String> values;
	
	public PromptedField(String name, String type, List<String> values) {
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
