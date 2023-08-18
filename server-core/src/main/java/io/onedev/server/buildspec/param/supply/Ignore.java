package io.onedev.server.buildspec.param.supply;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.annotation.Editable;

@Editable(name="Ignore this param")
public class Ignore implements ValuesProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Ignore this param";
	
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
	public List<List<String>> getValues(Build build, ParamCombination paramCombination) {
		List<List<String>> values = new ArrayList<>();
		values.add(new ArrayList<>());
		return values;
	}

}
