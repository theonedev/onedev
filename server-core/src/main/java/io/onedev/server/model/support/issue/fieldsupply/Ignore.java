package io.onedev.server.model.support.issue.fieldsupply;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class Ignore implements ValueProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Ignore";
	
	@Override
	public boolean equals(Object other) {
		return other instanceof Ignore; 
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
