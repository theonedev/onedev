package io.onedev.server.util.inputspec.choiceinput.defaultmultivalueprovider;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.GroovyUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Multiline;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.Script;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultMultiValue implements DefaultMultiValueProvider {

	private static final long serialVersionUID = 1L;

	private String script;

	@Editable(description="Groovy script to be evaluated. It should return list of string. "
			+ "Check <a href='$docRoot/Scripting' target='_blank'>scripting help</a> for details")
	@NotEmpty
	@Script
	@OmitName
	@Multiline
	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getDefaultValue() {
		return (List<String>) GroovyUtils.evalScript(getScript());
	}

}
