package io.onedev.server.model.support.inputspec.userchoiceinput.defaultmultivalueprovider;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultMultiValue implements DefaultMultiValueProvider {

	private static final long serialVersionUID = 1L;

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. It should return list of user login name. "
			+ "Check <a href='$docRoot/pages/scripting.md' target='_blank'>scripting help</a> for details")
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
