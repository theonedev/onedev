package io.onedev.server.util.inputspec.booleaninput;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.booleaninput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.inputspec.booleaninput.defaultvalueprovider.FalseDefaultValue;

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
		appendField(buffer, index, "boolean");
		appendAnnotations(buffer, index, null, null, defaultValueProvider!=null);
		appendMethods(buffer, index, "boolean", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return Boolean.valueOf(strings.iterator().next());
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList(((Boolean)value)?"true":"false");
	}

}
