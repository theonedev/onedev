package io.onedev.server.util.inputspec.passwordinput;

import java.util.Map;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.passwordinput.defaultvalueprovider.DefaultValueProvider;

@Editable(order=500, name=InputSpec.PASSWORD)
public class PasswordInput extends InputSpec {

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
		appendField(buffer, index, "String");
		appendAnnotations(buffer, index, "NotEmpty", null, defaultValueProvider!=null);
		buffer.append("    @Password\n");
		appendMethods(buffer, index, "String", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object toObject(String string) {
		return string;
	}

	@Override
	public String toString(Object value) {
		return (String) value;
	}

}
