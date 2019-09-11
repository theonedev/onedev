package io.onedev.server.util.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Script;

@Editable(order=300, name="Evaluate script to get choices")
public class ScriptingChoices implements ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private List<String> script;

	@Editable(description="Groovy script to be evaluated. The return value should be a list of user facade object to be used as choices. "
			+ "Check <a href='$docRoot/Scripting' target='_blank'>scripting help</a> for details")
	@Script(Script.GROOVY)
	@OmitName
	@Size(min=1, message="may not be empty")
	public List<String> getScript() {
		return script;
	}

	public void setScript(List<String> script) {
		this.script = script;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<UserFacade> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		try {
			return (List<UserFacade>) GroovyUtils.evalScript(StringUtils.join(getScript(), "\n"), variables);
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
