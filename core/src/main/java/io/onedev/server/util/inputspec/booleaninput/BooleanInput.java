package io.onedev.server.util.inputspec.booleaninput;

import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.booleaninput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.inputspec.booleaninput.defaultvalueprovider.FalseDefaultValue;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name=InputSpec.BOOLEAN)
public class BooleanInput extends InputSpec {

	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider = new FalseDefaultValue();

	@Editable(name="Default Value", order=1000)
	@NotNull(message="may not be empty")
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public List<String> getPossibleValues() {
		return Lists.newArrayList("true", "false");
	}

	@Editable
	@Override
	public boolean isAllowEmpty() {
		return false;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "Boolean");
		appendCommonAnnotations(buffer, index);
		buffer.append("    @NotNull\n");
		appendMethods(buffer, index, "Boolean", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (strings.size() == 0) {
			throw new ValidationException("Invalid boolean value");
		} else if (strings.size() == 1) {
			String string = strings.iterator().next();
			if (string.equalsIgnoreCase("true"))
				return true;
			else if (string.equalsIgnoreCase("false"))
				return false;
			else
				throw new ValidationException("Invalid boolean value");
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	@Override
	public List<String> convertToStrings(Object value) {
		if (value instanceof Boolean)
			return Lists.newArrayList(((Boolean)value)?"true":"false");
		else
			return Lists.newArrayList("false");
	}

}
