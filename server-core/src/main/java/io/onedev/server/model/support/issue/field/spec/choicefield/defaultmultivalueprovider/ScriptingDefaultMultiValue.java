package io.onedev.server.model.support.issue.field.spec.choicefield.defaultmultivalueprovider;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ScriptChoice;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultMultiValue implements DefaultMultiValueProvider {

	private static final long serialVersionUID = 1L;

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. It should return list of string. "
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
	public List<String> getDefaultValue() {
		return (List<String>) GroovyUtils.evalScriptByName(scriptName);
	}

}
