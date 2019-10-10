package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.inputspec.booleaninput.BooleanInput;
import io.onedev.server.model.support.inputspec.booleaninput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.model.support.inputspec.booleaninput.defaultvalueprovider.FalseDefaultValue;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name=FieldSpec.BOOLEAN)
public class BooleanField extends FieldSpec {

	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider = new FalseDefaultValue();

	@Editable(name="Default Value", order=1000)
	@NotNull(message="may not be empty")
	@Valid
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
		return BooleanInput.getPossibleValues();
	}

	@Editable
	@Override
	public boolean isAllowEmpty() {
		return false;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return BooleanInput.getPropertyDef(this, indexes, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return BooleanInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return BooleanInput.convertToStrings(value);
	}

}
