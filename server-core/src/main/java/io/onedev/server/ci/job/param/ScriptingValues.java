package io.onedev.server.ci.job.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Script;

@Editable
public class ScriptingValues implements ValuesProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Evaluate script to get values";
	
	public static final String SECRET_DISPLAY_NAME = "Evaluate script to get secrets";
	
	private List<String> script;
	
	@Editable
	@Script(Script.GROOVY)
	@Size(min=1, message="may not be empty")
	public List<String> getScript() {
		return script;
	}

	public void setScript(List<String> script) {
		this.script = script;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ScriptingValues)) 
			return false;
		if (this == other)
			return true;
		ScriptingValues otherScriptingValues = (ScriptingValues) other;
		return new EqualsBuilder()
			.append(script, otherScriptingValues.script)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(script)
			.toHashCode();
	}		
	
	@SuppressWarnings("unchecked")
	@Override
	public List<List<String>> getValues() {
		Map<String, Object> variables = new HashMap<>();
		List<List<String>> values = new ArrayList<>();
		for (Object each: (List<Object>) GroovyUtils.evalScript(StringUtils.join(getScript(), "\n"), variables)) {
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
