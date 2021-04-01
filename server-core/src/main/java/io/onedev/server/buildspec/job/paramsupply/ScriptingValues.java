package io.onedev.server.buildspec.job.paramsupply;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Build;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(name="Evaluate script to get values or secrets")
public class ScriptingValues implements ValuesProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Evaluate script to get values";
	
	public static final String SECRET_DISPLAY_NAME = "Evaluate script to get secrets";
	
	private String scriptName;
	
	@Editable
	@ScriptChoice
	@NotEmpty
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ScriptingValues)) 
			return false;
		if (this == other)
			return true;
		ScriptingValues otherScriptingValues = (ScriptingValues) other;
		return new EqualsBuilder()
			.append(scriptName, otherScriptingValues.scriptName)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(scriptName)
			.toHashCode();
	}		
	
	@SuppressWarnings("unchecked")
	@Override
	public List<List<String>> getValues(Build build, String paramName) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("build", build);
		List<List<String>> values = new ArrayList<>();
		Object result = GroovyUtils.evalScriptByName(scriptName, variables);
		if (result instanceof List) {
			for (Object each: (List<Object>) result) {
				List<String> strings = new ArrayList<>();
				if (each instanceof Collection) { 
					for (Object each2: (Collection<?>) each)
						strings.add(each2.toString());
				} else if (each != null) {
					strings.add(each.toString());
				}
				values.add(strings);
			}
		} else {
			List<String> strings = new ArrayList<>();
			if (result != null) 
				strings.add(result.toString());
			values.add(strings);
		}
		return values;
	}

}
