package io.onedev.server.util.inputspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.google.common.collect.Lists;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name=InputSpec.SECRET)
public class SecretInput extends InputSpec {

	private static final long serialVersionUID = 1L;

	public static final String MASK = "*****";
	
	public static final String LITERAL_VALUE_PREFIX = "$OneDev-Secret-Literal$";
	
	@Editable
	@Override
	public boolean isAllowEmpty() {
		return false;
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
		buffer.append("    @NotEmpty\n");
		buffer.append("    @Password\n");
		appendMethods(buffer, index, "String", null, null);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (strings.size() == 0)
			return null;
		else if (strings.size() == 1)
			return strings.iterator().next();
		else
			throw new ValidationException("Not eligible for multi-value");
	}

	@Override
	public List<String> convertToStrings(Object value) {
		if (value instanceof String)
			return Lists.newArrayList((String)value);
		else
			return new ArrayList<>();
	}

}
