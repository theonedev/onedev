package io.onedev.server.ci.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.Input;

public class VariableInterpolator implements Function<String, String> {

	public static final String PARAMS_PREFIX = "params:"; 
	
	public static final String SECRETS_PREFIX = "secrets:";
	
	public static final String SCRIPTS_PREFIX = "scripts:";
	
	private final Build build;
	
	public VariableInterpolator(Build build) {
		this.build = build;
	}
	
	@Override
	public String apply(String t) {
		for (JobVariable var: JobVariable.values()) {
			if (var.name().toLowerCase().equals(t))
				return var.getValue(build);
		}
		if (t.startsWith(PARAMS_PREFIX)) {
			String paramName = t.substring(PARAMS_PREFIX.length());
			for (Entry<String, Input> entry: build.getParamInputs().entrySet()) {
				if (paramName.equals(entry.getKey())) {
					if (build.isParamVisible(paramName)) {
						String paramType = entry.getValue().getType();
						List<String> paramValues = new ArrayList<>();
						for (String value: entry.getValue().getValues()) {
							if (paramType.equals(ParamSpec.SECRET)) 
								value = build.getSecretValue(value);
							paramValues.add(value);
						}
						return StringUtils.join(paramValues, "\n");
					} else {
						throw new OneException("Invisible param: " + paramName);
					}
				}					
			}
			throw new OneException("Undefined param: " + paramName);
		} else if (t.startsWith(SECRETS_PREFIX)) {
			String secretName = t.substring(SECRETS_PREFIX.length());
			return build.getSecretValue(secretName);
		} else if (t.startsWith(SCRIPTS_PREFIX)) {
			String scriptName = t.substring(SCRIPTS_PREFIX.length());
			Map<String, Object> context = new HashMap<>();
			context.put("build", build);
			Object result = GroovyUtils.evalScriptByName(scriptName, context);
			if (result != null)
				return result.toString();
			else
				return "";
		} else {
			throw new OneException("Unrecognized interpolation variable: " + t);
		}
	}

}
