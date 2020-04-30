package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.model.support.inputspec.numberinput.NumberInput;
import io.onedev.server.model.support.inputspec.numberinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=400, name=FieldSpec.NUMBER)
public class NumberField extends FieldSpec {
	
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
	@Valid
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return NumberInput.getPropertyDef(this, indexes, minValue, maxValue, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return NumberInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return NumberInput.convertToStrings(value);
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
