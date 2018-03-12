package io.onedev.server.util.input.dateinput;

import java.util.Map;

import org.joda.time.DateTime;

import io.onedev.server.util.Constants;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.dateinput.defaultvalueprovider.DefaultValueProvider;

@Editable(order=505, name=Input.DATE)
public class DateInput extends Input {

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
		appendAnnotations(buffer, index, "NotNull", null, defaultValueProvider!=null);
		appendMethods(buffer, index, "Date", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object toObject(String string) {
		if (string != null)
			return Constants.DATE_FORMATTER.parseDateTime(string).toDate();
		else
			return null;
	}

	@Override
	public String toString(Object value) {
		if (value != null)
			return Constants.DATE_FORMATTER.print(new DateTime(value));
		else
			return null;
	}

}
