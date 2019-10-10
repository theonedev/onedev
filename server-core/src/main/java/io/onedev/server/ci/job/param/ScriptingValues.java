package io.onedev.server.ci.job.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable
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
	public List<List<String>> getValues() {
		Map<String, Object> variables = new HashMap<>();
		List<List<String>> values = new ArrayList<>();
		for (Object each: (List<Object>) GroovyUtils.evalScriptByName(scriptName, variables)) {
			List<String> strings = new ArrayList<>();
			if (each instanceof Collection) { 
				strings.addAll((Collection<? extends String>) each);
				Collections.sort(strings);
			} else {
				strings.add((String) each);
			}
			values.add(strings);
		}
		return values;
	}

}
