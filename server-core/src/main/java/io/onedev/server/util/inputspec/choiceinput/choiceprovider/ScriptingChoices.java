package io.onedev.server.util.inputspec.choiceinput.choiceprovider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Script;

@Editable(order=300, name="Evaluate script to get choices")
public class ScriptingChoices extends ChoiceProvider {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ScriptingChoices.class);

	private List<String> script;

	@Editable(description="Groovy script to be evaluated. The return value should be a value to color map, "
			+ "for instance:<br>"
			+ "<code>return [\"Successful\":\"#00ff00\", \"Failed\":\"#ff0000\"]</code>, "
			+ "Use <tt>null</tt> if the value does not have a color. Check <a href='$docRoot/Scripting' target='_blank'>scripting help</a> for details")
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
	public Map<String, String> getChoices(boolean allPossible) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("allPossible", allPossible);
		try {
			Object result = GroovyUtils.evalScript(StringUtils.join(getScript(), "\n"), variables);
			if (result instanceof Map) {
				return (Map<String, String>) result;
			} else if (result instanceof List) {
				Map<String, String> choices = new HashMap<>();
				for (String item: (List<String>)result)
					choices.put(item, null);
				return choices;
			} else {
				throw new OneException("Script should return either a Map or a List");
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
