package io.onedev.server.model.support.issue.fieldsupply;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.server.model.Build;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable
public class ScriptingValue implements ValueProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Evaluate script to get value";
	
	public static final String SECRET_DISPLAY_NAME = "Evaluate script to get secret";
	
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
		if (!(other instanceof ScriptingValue)) 
			return false;
		if (this == other)
			return true;
		ScriptingValue otherScriptingValue = (ScriptingValue) other;
		return new EqualsBuilder()
			.append(scriptName, otherScriptingValue.scriptName)
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
	public List<String> getValue() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("build", Build.get());
		List<String> values = new ArrayList<>();
		Object result = GroovyUtils.evalScriptByName(scriptName, variables);
		if (result instanceof Collection) {
			values.addAll((Collection<? extends String>) result);
			Collections.sort(values);
		} else if (result != null) {
			values.add(result.toString());
		}
		return values;
	}

}
