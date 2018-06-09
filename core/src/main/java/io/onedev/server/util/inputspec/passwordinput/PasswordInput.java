package io.onedev.server.util.inputspec.passwordinput;

import java.util.List;
import java.util.Map;

import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.passwordinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import jersey.repackaged.com.google.common.collect.Lists;

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

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes, boolean setDefaultValue) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		buffer.append("    @Password\n");
		appendMethods(buffer, index, "String", null, setDefaultValue?defaultValueProvider:null);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return strings.iterator().next();
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList((String)value);
	}

}
