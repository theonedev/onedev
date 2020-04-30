package io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ScriptChoice;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. It should return a <i>string</i> value. "
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

	@Override
	public String getDefaultValue() {
		return (String) GroovyUtils.evalScriptByName(scriptName);
	}

}
