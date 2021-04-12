package io.onedev.server.util.interpolative;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.Input;
import io.onedev.server.util.interpolative.Interpolative.Segment;
import io.onedev.server.util.interpolative.Interpolative.Segment.Type;
import io.onedev.server.web.editable.annotation.Editable;

public class VariableInterpolator {

	public static final String HELP = "<b>NOTE: </b> Type <tt>@</tt> to <a href='$docRoot/pages/variable-substitution.md' target='_blank' tabindex='-1'>insert variable</a>. "
			+ "Use <tt>@@</tt> for literal <tt>@</tt>";
	
	public static final String PREFIX_PARAMS = "params:"; 
	
	public static final String PREFIX_PROPERTIES = "properties:";
	
	public static final String PREFIX_SECRETS = "secrets:";
	
	public static final String PREFIX_SCRIPTS = "scripts:";
	
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
				if (t.startsWith(PREFIX_PARAMS)) {
					String paramName = t.substring(PREFIX_PARAMS.length());
					for (Entry<String, Input> entry: paramCombination.getParamInputs().entrySet()) {
						if (paramName.equals(entry.getKey())) {
							if (paramCombination.isParamVisible(paramName)) {
								String paramType = entry.getValue().getType();
								List<String> paramValues = new ArrayList<>();
								for (String value: entry.getValue().getValues()) {
									if (paramType.equals(ParamSpec.SECRET)) 
										value = build.getSecretValue(value);
									paramValues.add(value);
								}
								return StringUtils.join(paramValues, ",");
							} else {
								throw new ExplicitException("Invisible param: " + paramName);
							}
						}					
					}
					throw new ExplicitException("Undefined param: " + paramName);
				} else if (t.startsWith(PREFIX_PROPERTIES)) {
					String propertyName = t.substring(PREFIX_PROPERTIES.length());
					Property property = build.getSpec().getPropertyMap().get(propertyName);
					if (property != null)
						return property.getValue();
					else
						throw new ExplicitException("Undefined property: " + propertyName);
				} else if (t.startsWith(PREFIX_SECRETS)) {
					String secretName = t.substring(PREFIX_SECRETS.length());
					return build.getSecretValue(secretName);
				} else if (t.startsWith(PREFIX_SCRIPTS)) {
					String scriptName = t.substring(PREFIX_SCRIPTS.length());
					Map<String, Object> context = new HashMap<>();
					context.put("build", build);
					Object result = GroovyUtils.evalScriptByName(scriptName, context);
					if (result != null)
						return result.toString();
					else
						return "";
				} else {
					throw new ExplicitException("Unrecognized interpolation variable: " + t);
				}
			}
			
		});
	}
	
	public VariableInterpolator(Function<String, String> variableResolver) {
		this.variableResolver = variableResolver;
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
	
	@SuppressWarnings("unchecked")
	public <T> T interpolateProperties(T object) {
		Class<T> clazz = (Class<T>) object.getClass();
		
		T interpolated;
		try {
			interpolated = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		for (Field field: BeanUtils.findFields(clazz)) {
			field.setAccessible(true);
			try {
				field.set(interpolated, field.get(object));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		for (Method getter: BeanUtils.findGetters(clazz)) {
			if (getter.getAnnotation(Editable.class) != null) {
				Method setter = BeanUtils.findSetter(getter);
				if (setter != null) {
					try {
						Object propertyValue = getter.invoke(interpolated);
						if (propertyValue != null) {
							Class<?> propertyClass = propertyValue.getClass();
							if (getter.getAnnotation(io.onedev.server.web.editable.annotation.Interpolative.class) != null) {
								try {
									if (propertyValue instanceof String) {
										setter.invoke(interpolated, interpolate((String) propertyValue));
									} else if (propertyValue instanceof List) {
										List<Object> interpolatedList = new ArrayList<>();
										for (Object element: (List<String>) propertyValue) { 
											if (element == null) {
												interpolatedList.add(element);
											} else if (element instanceof String) {
												interpolatedList.add(interpolate((String) element));
											} else if (element instanceof List) {
												List<String> interpolatedList2 = new ArrayList<>();
												for (String element2: (List<String>) element)  
													interpolatedList2.add(interpolate(element2));
												interpolatedList.add(interpolatedList2);
											} else {
												throw new RuntimeException("Unexpected list element type: " + element.getClass());
											}
										}
										setter.invoke(interpolated, interpolatedList);
									}
								} catch (Exception e) {
									String message = String.format("Error interpolating (class: %s, property: %s)", 
											propertyClass, BeanUtils.getPropertyName(getter));
									throw new RuntimeException(message, e);
								}
							} else if (propertyClass.getAnnotation(Editable.class) != null) {
								setter.invoke(interpolated, interpolateProperties(propertyValue));
							} else if (propertyValue instanceof List) {
								List<Object> interpolatedList = new ArrayList<>();
								for (Object element: (List<?>)propertyValue) { 
									if (element != null && element.getClass().getAnnotation(Editable.class) != null)
										interpolatedList.add(interpolateProperties(element));
									else
										interpolatedList.add(element);
								}
								setter.invoke(interpolated, interpolatedList);
							}
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		return interpolated;
	}	
		
}
