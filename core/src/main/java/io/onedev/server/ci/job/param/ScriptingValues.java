package io.onedev.server.ci.job.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import edu.emory.mathcs.backport.java.util.Collections;
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
	public List<List<String>> getValues() {
		Map<String, Object> variables = new HashMap<>();
		List<List<String>> values = new ArrayList<>();
		for (Object each: (List<Object>) GroovyUtils.evalScript(getScript(), variables)) {
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
