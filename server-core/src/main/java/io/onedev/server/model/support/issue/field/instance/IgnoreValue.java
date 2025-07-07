package io.onedev.server.model.support.issue.field.instance;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.server.annotation.Editable;

@Editable(name="Ignore this field")
public class IgnoreValue implements ValueProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Ignore this field";
	
	@Override
	public boolean equals(Object other) {
		return other instanceof IgnoreValue; 
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.toHashCode();
	}		
	
	@Override
	public List<String> getValue() {
		return new ArrayList<>();
	}

}
