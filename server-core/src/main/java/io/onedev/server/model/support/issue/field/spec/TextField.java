package io.onedev.server.model.support.issue.field.spec;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.onedev.server.buildspecmodel.inputspec.textinput.TextInput;
import io.onedev.server.buildspecmodel.inputspec.textinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.annotation.Editable;

@Editable(order=100, name=FieldSpec.TEXT)
public class TextField extends FieldSpec {

	private static final long serialVersionUID = 1L;

	private boolean multiline;
	
	private String pattern;
	
	private DefaultValueProvider defaultValueProvider;

	@Editable(order=1050, name="Multiple Lines")
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

	@Editable(order=1200, name="Default Value", placeholder="No default value")
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
	public String getPropertyDef(Map<String, Integer> indexes) {
		return TextInput.getPropertyDef(this, indexes, pattern, multiline, defaultValueProvider);
	}

	@Override
	public Object convertToObject(List<String> strings) {
		return TextInput.convertToObject(strings);
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return TextInput.convertToStrings(value);
	}

	@Override
	protected void runScripts() {
		if (getDefaultValueProvider() != null)
			getDefaultValueProvider().getDefaultValue();
	}
	
}
