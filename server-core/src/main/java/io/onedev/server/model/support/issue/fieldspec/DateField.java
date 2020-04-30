package io.onedev.server.model.support.issue.fieldspec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.model.support.inputspec.dateinput.DateInput;
import io.onedev.server.model.support.inputspec.dateinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.Constants;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=505, name=FieldSpec.DATE)
public class DateField extends FieldSpec {

	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Default Value")
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
		return DateInput.getPropertyDef(this, indexes, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return DateInput.convertToObject(strings);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return DateInput.convertToStrings(value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null) 
			return Constants.DATE_FORMATTER.parseDateTime(fieldValue).toDate().getTime();
		else
			return super.getOrdinal(fieldValue);
	}

}
