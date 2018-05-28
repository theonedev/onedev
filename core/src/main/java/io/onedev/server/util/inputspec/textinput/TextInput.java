package io.onedev.server.util.inputspec.textinput;

import java.util.List;
import java.util.Map;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.textinput.defaultvalueprovider.DefaultValueProvider;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable(order=100, name=InputSpec.TEXT)
public class TextInput extends InputSpec {

	private static final long serialVersionUID = 1L;

	private boolean multiline;
	
	private String pattern;
	
	private DefaultValueProvider defaultValueProvider;

	@Editable(order=1000, description="Enable for multi-line input")
	public boolean isMultiline() {
		return multiline;
	}

	public void setMultiline(boolean multiline) {
		this.multiline = multiline;
	}

	@Editable(order=1100, description="Optionally specify a <a href='http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html'>regular expression pattern</a> for valid values of " +
			"the text input")
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	@Editable(order=1200, name="Default Value")
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
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		if (defaultValueProvider != null)
			appendDefaultValueProvider(buffer, index);
		if (isMultiline())
			buffer.append("    @Multiline\n");
		if (getPattern() != null)
			buffer.append("    @Pattern(regexp=\"" + pattern + "\", message=\"Should match regular expression: " + pattern + "\")\n");
		appendMethods(buffer, index, "String", null, defaultValueProvider);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return strings.iterator().next();
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList((String) value);
	}
	
}
