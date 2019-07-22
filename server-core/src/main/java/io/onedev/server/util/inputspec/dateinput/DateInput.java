package io.onedev.server.util.inputspec.dateinput;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.joda.time.DateTime;

import com.google.common.collect.Lists;

import io.onedev.server.util.Constants;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.dateinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=505, name=InputSpec.DATE)
public class DateInput extends InputSpec {

	private static final long serialVersionUID = 1L;

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Default Value")
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
		appendField(buffer, index, "Date");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotNull(message=\"May not be empty\")\n");
		appendMethods(buffer, index, "Date", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (strings.size() == 1)
			return Constants.DATE_FORMATTER.parseDateTime(strings.iterator().next()).toDate();
		else if (strings.size() == 0)
			return null;
		else
			throw new ValidationException("Not eligible for multi-value");
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public List<String> convertToStrings(Object value) {
		if (value != null)
			return Lists.newArrayList(Constants.DATE_FORMATTER.print(new DateTime(value)));
		else
			return new ArrayList<>();
	}

	@Override
	public long getOrdinal(Object fieldValue) {
		if (fieldValue instanceof Date) 
			return ((Date)fieldValue).getTime();
		else
			return super.getOrdinal(fieldValue);
	}

}
