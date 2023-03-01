package io.onedev.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.model.Group;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ScriptChoice;

@Editable(order=300, name="Evaluate script to get choices")
public class ScriptingChoices implements ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. The return value should be a list of group facade object to be used as choices. "
			+ "Check <a href='https://docs.onedev.io/appendix/scripting' target='_blank'>scripting help</a> for details")
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
	public List<Group> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		try {
			return (List<Group>) GroovyUtils.evalScriptByName(scriptName, variables);
		} catch (RuntimeException e) {
			if (allPossible) {
				logger.error("Error getting all possible choices", e);
				return new ArrayList<>();
			} else {
				throw e;
			}
		}
	}

}
