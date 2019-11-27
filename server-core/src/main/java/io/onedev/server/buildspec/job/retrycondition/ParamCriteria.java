package io.onedev.server.buildspec.job.retrycondition;

import java.util.List;
import java.util.function.Predicate;

import io.onedev.server.model.Build;

public class ParamCriteria implements Predicate<Build> {

	private String name;
	
	private String value;
	
	public ParamCriteria(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean test(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		if (paramValues == null || paramValues.isEmpty())
			return value == null;
		else 
			return paramValues.contains(value);
	}

}
