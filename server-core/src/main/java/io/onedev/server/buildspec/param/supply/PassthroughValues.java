package io.onedev.server.buildspec.param.supply;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import javax.validation.constraints.NotEmpty;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.Input;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable(name="Use value of specified parameter/secret")
public class PassthroughValues implements ValuesProvider {

	private static final long serialVersionUID = 1L;

	public static final String DISPLAY_NAME = "Use value of specified parameter";
	
	private String paramName;
	
	@Editable
	@ChoiceProvider("getParamChoices")
	@NotEmpty
	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	@SuppressWarnings("unused")
	private static List<String> getParamChoices() {
		List<ParamSpec> paramSpecs = ParamSpec.list();
		if (paramSpecs != null) 
			return paramSpecs.stream().map(it->it.getName()).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}
	
	@Override
	public List<List<String>> getValues(Build build, ParamCombination paramCombination) {
		if (paramCombination != null) {
			Input param = paramCombination.getParamInputs().get(paramName);
			if (param != null) {
				List<List<String>> values = new ArrayList<>();
				values.add(param.getValues());
				return values;
			} else {
				String message = String.format("Param not found: %s", paramName);
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
		PassthroughValues otherPassthroughValues = (PassthroughValues) other;
		return new EqualsBuilder()
			.append(paramName, otherPassthroughValues.paramName)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(paramName)
			.toHashCode();
	}		

}
