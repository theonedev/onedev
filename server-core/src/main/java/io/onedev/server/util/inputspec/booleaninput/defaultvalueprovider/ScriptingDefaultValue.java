package io.onedev.server.util.inputspec.booleaninput.defaultvalueprovider;

import java.util.List;

import javax.validation.constraints.Size;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Script;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private List<String> script;

	@Editable(description="Groovy script to be evaluated. It should return a <i>boolean</i> value. "
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

	@Override
	public boolean getDefaultValue() {
		return (boolean) GroovyUtils.evalScript(StringUtils.join(getScript(), "\n"));
	}

}
