package io.onedev.server.buildspec.param.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.buildspecmodel.inputspec.datetimeinput.DateTimeInput;
import io.onedev.server.buildspecmodel.inputspec.datetimeinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.Constants;
import io.onedev.server.annotation.Editable;

@Editable(order=600, name=ParamSpec.DATE_TIME)
public class DateTimeParam extends ParamSpec {

	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Default Value", placeholder="No default value")
	@Valid
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		return DateTimeInput.getPropertyDef(this, indexes, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return DateTimeInput.convertToObject(strings);
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return DateTimeInput.convertToStrings(value);
	}

	@Override
	public long getOrdinal(String fieldValue) {
		if (fieldValue != null) 
			return Constants.DATETIME_FORMATTER.parseDateTime(fieldValue).toDate().getTime();
		else
			return super.getOrdinal(fieldValue);
	}

}
