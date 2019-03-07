package io.onedev.server.util.inputspec.choiceinput.choiceprovider;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Script;

@Editable(order=300, name="Evaluate script to get choices")
public class ScriptingChoices extends ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private String script;

	@Editable(description="Groovy script to be evaluated. The return value should be a value to color map, "
			+ "for instance:<br>"
			+ "<code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, "
			+ "Use <tt>null</tt> if the value does not have a color. Check <a href='$docRoot/Scripting' target='_blank'>scripting help</a> for details")
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
	public Map<String, String> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		try {
			return (Map<String, String>) GroovyUtils.evalScript(getScript(), variables);
		} catch (RuntimeException e) {
			if (allPossible) {
				logger.error("Error getting all possible choices", e);
				return new HashMap<>();
			} else {
				throw e;
			}
		}
	}

}
