package io.onedev.server.buildspec.param.instance;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ScriptChoice;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.model.Build;
import io.onedev.server.util.GroovyUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.NotEmpty;
import java.util.*;

@Editable(name="Evaluate script to get value or secret")
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
		ScriptingValue otherScriptingValues = (ScriptingValue) other;
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
	
	@Override
	public List<String> getValue(Build build, ParamCombination paramCombination) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("build", build);
		if (paramCombination != null) 
			variables.put("params", paramCombination.getParamMap());
		
		List<String> value = new ArrayList<>();
		Object result = GroovyUtils.evalScriptByName(scriptName, variables);
		if (result instanceof List) {
			for (var each: (List<?>)result) 
				value.add(each.toString());
		} else if (result != null) {
			value.add(result.toString());
		}
		return value;
	}

}
