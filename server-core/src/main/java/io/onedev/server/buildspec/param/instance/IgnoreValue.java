package io.onedev.server.buildspec.param.instance;

import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

@Editable(name="Ignore this param")
public class IgnoreValue implements ValueProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Ignore this param";
	
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
	public List<String> getValue(Build build, ParamCombination paramCombination) {
		return new ArrayList<>();
	}

}
