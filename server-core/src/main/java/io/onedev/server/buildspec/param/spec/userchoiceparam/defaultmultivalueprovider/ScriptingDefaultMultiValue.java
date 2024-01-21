package io.onedev.server.buildspec.param.spec.userchoiceparam.defaultmultivalueprovider;

import com.google.common.collect.Lists;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ScriptChoice;
import io.onedev.server.util.GroovyUtils;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Editable(order=400, name="Evaluate script to get default value")
public class ScriptingDefaultMultiValue implements DefaultMultiValueProvider {

	private static final long serialVersionUID = 1L;

	private String scriptName;

	@Editable(description="Groovy script to be evaluated. It should return string or list of string. "
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
		var result = GroovyUtils.evalScriptByName(scriptName);
		if (result instanceof List)
			return (List<String>) result;
		else if (result instanceof String)
			return Lists.newArrayList((String)result);
		else
			return new ArrayList<>();
	}

}
