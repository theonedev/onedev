package io.onedev.server.buildspecmodel.inputspec.dateinput.defaultvalueprovider;

import java.util.Date;

import javax.validation.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ScriptChoice;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. It should return a <i>Date</i> value. "
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

	@Override
	public Date getDefaultValue() {
		return (Date) GroovyUtils.evalScriptByName(scriptName);
	}

}
