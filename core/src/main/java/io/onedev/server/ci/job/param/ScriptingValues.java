package io.onedev.server.ci.job.param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Script;

@Editable(order=200, name="Evaluate script to get values")
public class ScriptingValues implements ValueProvider {

	private static final long serialVersionUID = 1L;
	
	private String script;

	@Editable(description="Groovy script to be evaluated. The return value should be a list of string with "
			+ "each item representing a separate value")
	@NotEmpty
	@Script
	@OmitName
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getValues() {
		Map<String, Object> variables = new HashMap<>();
		return (List<String>) GroovyUtils.evalScript(getScript(), variables);
	}

}
