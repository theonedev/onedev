package io.onedev.server.ci.job.param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Script;

@Editable
public class ScriptingValues implements ValuesProvider {

	private static final long serialVersionUID = 1L;
	
	public static final String DISPLAY_NAME = "Use return values of a script";
	
	private String script;

	@Editable
	@NotEmpty
	@Script
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
