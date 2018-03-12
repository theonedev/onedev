package io.onedev.server.util.input.textinput;

import java.util.Map;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;
import io.onedev.server.util.input.Input;
import io.onedev.server.util.input.textinput.defaultvalueprovider.DefaultValueProvider;

@Editable(order=100, name=Input.TEXT)
public class TextInput extends Input {

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

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendAnnotations(buffer, index, "NotEmpty", null, defaultValueProvider!=null);
		if (isMultiline())
			buffer.append("    @Multiline\n");
		if (getPattern() != null)
			buffer.append("    @Pattern(regexp=\"" + pattern + "\", message=\"Should match regular expression: " + pattern + "\")\n");
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
