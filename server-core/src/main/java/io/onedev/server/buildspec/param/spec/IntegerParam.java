package io.onedev.server.buildspec.param.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.buildspecmodel.inputspec.integerinput.IntegerInput;
import io.onedev.server.buildspecmodel.inputspec.integerinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.annotation.Editable;

@Editable(order=400, name=ParamSpec.INTEGER)
public class IntegerParam extends ParamSpec {
	
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

	@Editable(order=1200, name="Default Value", placeholder="No default value")
	@Valid
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return IntegerInput.getPropertyDef(this, indexes, minValue, maxValue, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return IntegerInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return IntegerInput.convertToStrings(value);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return Integer.parseInt(fieldValue);
		else
			return super.getOrdinal(fieldValue);
	}
	
}
