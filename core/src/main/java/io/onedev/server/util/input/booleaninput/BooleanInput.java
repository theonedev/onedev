package io.onedev.server.util.input.booleaninput;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.booleaninput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.input.booleaninput.defaultvalueprovider.FalseDefaultValue;

@Editable(order=300, name=Input.BOOLEAN)
public class BooleanInput extends Input {

	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider = new FalseDefaultValue();

	@Editable(name="Default Value", order=1000)
	@NotNull
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
		appendField(buffer, index, "Boolean");
		appendAnnotations(buffer, index, "NotNull", null, defaultValueProvider!=null);
		appendMethods(buffer, index, "Boolean", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object toObject(String string) {
		return Boolean.valueOf(string);
	}

	@Override
	public String toString(Object value) {
		if (value != null)
			return ((Boolean)value)?"true":"false";
		else
			return "false";
	}

}
