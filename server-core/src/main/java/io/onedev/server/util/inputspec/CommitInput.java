package io.onedev.server.util.inputspec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=1400, name=InputSpec.COMMIT)
public class CommitInput extends InputSpec {

	private static final long serialVersionUID = 1L;

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		appendMethods(buffer, index, "String", null, null);
		
		return buffer.toString();
	}

	@Override
	public Object convertToObject(List<String> strings) {
		if (strings.size() == 0) {
			return null;
		} else if (strings.size() == 1) {
			String value = strings.iterator().next();
			if (ObjectId.isId(value))
				return value;
			else
				throw new ValidationException("Invalid commit id");
		} else {
			throw new ValidationException("Not eligible for multi-value");
		}
	}

	@Override
	public List<String> convertToStrings(Object value) {
		if (value instanceof String)
			return Lists.newArrayList((String) value);
		else
			return new ArrayList<>();
	}
	
}
