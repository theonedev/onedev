package io.onedev.server.util.inputspec.dateinput;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
			buffer.append("    @NotNull\n");
		appendMethods(buffer, index, "Date", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return Constants.DATE_FORMATTER.parseDateTime(strings.iterator().next()).toDate();
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList(Constants.DATE_FORMATTER.print(new DateTime(value)));
	}

	@Override
	public long getOrdinal(Object fieldValue) {
		if (fieldValue != null) 
			return ((Date)fieldValue).getTime();
		else
			return super.getOrdinal(fieldValue);
	}

}
