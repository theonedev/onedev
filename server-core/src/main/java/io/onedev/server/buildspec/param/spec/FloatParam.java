package io.onedev.server.buildspec.param.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.buildspecmodel.inputspec.floatinput.FloatInput;
import io.onedev.server.buildspecmodel.inputspec.floatinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.annotation.Editable;

@Editable(order=450, name=ParamSpec.FLOAT)
public class FloatParam extends ParamSpec {
	
	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider;
	
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
		return FloatInput.getPropertyDef(this, indexes, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return FloatInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return FloatInput.convertToStrings(value);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null)
			return Float.valueOf(fieldValue).longValue();
		else
			return super.getOrdinal(fieldValue);
	}
	
}
