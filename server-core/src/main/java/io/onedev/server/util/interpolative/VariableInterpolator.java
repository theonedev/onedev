package io.onedev.server.util.interpolative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.buildspec.Property;
import io.onedev.server.buildspec.job.JobVariable;
import io.onedev.server.buildspec.param.ParamCombination;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.interpolative.Interpolative.Segment;
import io.onedev.server.util.interpolative.Interpolative.Segment.Type;
import io.onedev.server.web.editable.EditableStringTransformer;

import static io.onedev.k8shelper.KubernetesHelper.*;

public class VariableInterpolator {

	public static final String HELP = "<b>NOTE: </b> Type <tt>@</tt> to <a href='$docRoot/pages/variable-substitution.md' target='_blank' tabindex='-1'>insert variable</a>. "
			+ "Use <tt>@@</tt> for literal <tt>@</tt>";
	
	public static final String PREFIX_PARAM = "param:"; 
	
	public static final String PREFIX_PROPERTY = "property:";
	
	public static final String PREFIX_SECRET = "secret:";
	
	public static final String PREFIX_SCRIPT = "script:";
	
	public static final String PREFIX_FILE = "file:";
	
	private final EditableStringTransformer beanPropertyTransformer;
	
	private final Function<String, String> variableResolver;
	
	public VariableInterpolator(Build build, ParamCombination paramCombination) {
		this(new Function<String, String>() {

			@Override
			public String apply(String t) {
				for (JobVariable var: JobVariable.values()) {
					if (var.name().toLowerCase().equals(t)) {
						String value = var.getValue(build);
						return value!=null?value:"";
					}
				}
				if (t.startsWith(PREFIX_PARAM) || t.startsWith("params:")) {
					String paramName;
					if (t.startsWith(PREFIX_PARAM))
						paramName = t.substring(PREFIX_PARAM.length());
					else
						paramName = t.substring("params:".length());
						
					for (Entry<String, Input> entry: paramCombination.getParamInputs().entrySet()) {
						if (paramName.equals(entry.getKey())) {
							if (paramCombination.isParamVisible(paramName)) {
								String paramType = entry.getValue().getType();
								List<String> paramValues = new ArrayList<>();
								for (String value: entry.getValue().getValues()) {
									if (paramType.equals(ParamSpec.SECRET)) 
										value = build.getJobSecretAuthorizationContext().getSecretValue(value);
									paramValues.add(value);
								}
								return StringUtils.join(paramValues, ",");
							} else {
								throw new ExplicitException("Invisible param: " + paramName);
							}
						}					
					}
					throw new ExplicitException("Undefined param: " + paramName);
				} else if (t.startsWith(PREFIX_PROPERTY) || t.startsWith("properties:")) {
					String propertyName;
					if (t.startsWith(PREFIX_PROPERTY))
						propertyName = t.substring(PREFIX_PROPERTY.length());
					else
						propertyName = t.substring("properties:".length());
						
					Property property = build.getSpec().getPropertyMap().get(propertyName);
					if (property != null)
						return property.getValue();
					else
						throw new ExplicitException("Undefined property: " + propertyName);
				} else if (t.startsWith(PREFIX_SECRET) || t.startsWith("secrets:")) {
					String secretName;
					if (t.startsWith(PREFIX_SECRET))
						secretName = t.substring(PREFIX_SECRET.length());
					else
						secretName = t.substring("secrets:".length());
					return build.getJobSecretAuthorizationContext().getSecretValue(secretName);
				} else if (t.startsWith(PREFIX_SCRIPT) || t.startsWith("scripts:")) {
					String scriptName;
					if (t.startsWith(PREFIX_SCRIPT))
						scriptName = t.substring(PREFIX_SCRIPT.length());
					else
						scriptName = t.substring("scripts:".length());
					
					Map<String, Object> context = new HashMap<>();
					context.put("build", build);
					Object result = GroovyUtils.evalScriptByName(scriptName, context);
					if (result != null)
						return result.toString();
					else
						return "";
				} else if (t.startsWith(PREFIX_FILE)) {
					return PLACEHOLDER_PREFIX + WORKSPACE + "/" + t.substring(PREFIX_FILE.length()) + PLACEHOLDER_SUFFIX;
				} else {
					throw new ExplicitException("Unrecognized interpolation variable: " + t);
				}
			}
			
		});
	}
	
	public VariableInterpolator(Function<String, String> variableResolver) {
		this.variableResolver = variableResolver;
		beanPropertyTransformer = new EditableStringTransformer(new Function<String, String>() {

			@Override
			public String apply(String t) {
				return interpolate(t);
			}
			
		});
	}
	
	public VariableInterpolator(Build build) {
		this(build, build.getParamCombination());
	}

	public String interpolate(@Nullable String value) {
		if (value != null) {
			Interpolative interpolative = Interpolative.parse(value);
			StringBuilder builder = new StringBuilder();
			for (Segment segment: interpolative.getSegments(null)) {
				if (segment.getType() == Type.LITERAL) {
					builder.append(segment.getContent());
				} else {
					String interpolated = variableResolver.apply(segment.getContent()); 
					if (interpolated != null)
						builder.append(interpolated);
				}
			}
			return builder.toString();
		} else {
			return null;
		}
	}
	
	public <T> T interpolateProperties(T object) {
		return beanPropertyTransformer.transformProperties(
				object, 
				io.onedev.server.web.editable.annotation.Interpolative.class);
	}	
		
}
