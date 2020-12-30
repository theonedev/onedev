package io.onedev.server.model.support.inputspec.choiceinput.choiceprovider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(order=300, name="Evaluate script to get choices")
public class ScriptingChoices extends ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. The return value should be a value to color map, "
			+ "for instance:<br>"
			+ "<code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, "
			+ "Use <tt>null</tt> if the value does not have a color. Check <a href='$docRoot/pages/scripting.md' target='_blank'>scripting help</a> for details")
	@ScriptChoice
	@OmitName
	@NotEmpty
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		
		try {
			Object result = GroovyUtils.evalScriptByName(scriptName, variables);
			if (result instanceof Map) {
				return (Map<String, String>) result;
			} else if (result instanceof List) {
				Map<String, String> choices = new HashMap<>();
				for (String item: (List<String>)result)
					choices.put(item, null);
				return choices;
			} else {
				throw new ExplicitException("Script should return either a Map or a List");
			}
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
