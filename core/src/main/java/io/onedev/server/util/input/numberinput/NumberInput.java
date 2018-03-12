package io.onedev.server.util.input.numberinput;

import java.util.Map;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.numberinput.defaultvalueprovider.DefaultValueProvider;

@Editable(order=400, name=Input.NUMBER)
public class NumberInput extends Input {
	
	private static final long serialVersionUID = 1L;

	private Integer minValue;
	
	private Integer maxValue;
	
	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, description="Optionally specify the minimum value allowed.")
	public Integer getMinValue() {
		return minValue;
	}

	public void setMinValue(Integer minValue) {
		this.minValue = minValue;
	}

	@Editable(order=1100, description="Optionally specify the maximum value allowed.")
	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}

	@Editable(order=1200, name="Default Value")
	@NameOfEmptyValue("No default value")
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "Integer");
		appendAnnotations(buffer, index, "NotNull", null, defaultValueProvider!=null);
		if (minValue != null) {
			if (maxValue != null) {
				buffer.append("    @Range(min=" + minValue.toString() + "L,max=" + maxValue.toString() +"L)\n");
			} else {
				buffer.append("    @Range(min=" + minValue.toString() + "L)\n");
			}
		} else if (maxValue != null) {
			buffer.append("    @Range(max=" + maxValue.toString() + "L)\n");
		}
		appendMethods(buffer, index, "Integer", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object toObject(String string) {
		if (string != null)
			return Integer.valueOf(string);
		else
			return null;
	}

	@Override
	public String toString(Object value) {
		if (value != null)
			return String.valueOf(value);
		else
			return null;
	}
}
