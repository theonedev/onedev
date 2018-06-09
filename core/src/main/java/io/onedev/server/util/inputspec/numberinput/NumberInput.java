package io.onedev.server.util.inputspec.numberinput;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.numberinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=400, name=InputSpec.NUMBER)
public class NumberInput extends InputSpec {
	
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
	public String getPropertyDef(Map<String, Integer> indexes, boolean setDefaultValue) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "Integer");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotNull\n");
		if (minValue != null) {
			if (maxValue != null) {
				buffer.append("    @Range(min=" + minValue.toString() + "L,max=" + maxValue.toString() +"L)\n");
			} else {
				buffer.append("    @Range(min=" + minValue.toString() + "L)\n");
			}
		} else if (maxValue != null) {
			buffer.append("    @Range(max=" + maxValue.toString() + "L)\n");
		}
		appendMethods(buffer, index, "Integer", null, setDefaultValue?defaultValueProvider:null);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return Integer.valueOf(strings.iterator().next());
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList(String.valueOf(value));
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public long getOrdinal(OneContext context, Object fieldValue) {
		if (fieldValue != null)
			return (Integer)fieldValue;
		else
			return super.getOrdinal(context, fieldValue);
	}
	
}
