package io.onedev.server.buildspec.job.paramsupply;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Build;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="Use specified values or job secrets")
public class PassthroughValues implements ValuesProvider {

	private static final long serialVersionUID = 1L;

	public static final String DISPLAY_NAME = "Use same parameter as current build";
	
	public static final String SECRET_DISPLAY_NAME = "Use same secret as current build";

	@Override
	public List<List<String>> getValues(Build build, String paramName) {
		if (build != null) {
			List<String> paramValue = build.getParamMap().get(paramName);
			if (paramValue != null) {
				List<List<String>> values = new ArrayList<>();
				values.add(paramValue);
				return values;
			} else {
				String message = String.format("Param not found (build: %s, param: %s)", build.getFQN(), paramName);
				throw new ExplicitException(message);
			}
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof PassthroughValues)) 
			return false;
		if (this == other)
			return true;
		else
			return false;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.toHashCode();
	}

}
