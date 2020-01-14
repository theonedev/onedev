package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneException;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.model.Build;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.GroovyUtils;
import io.onedev.server.util.Input;
import io.onedev.server.web.editable.annotation.Editable;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class VariableInterpolator implements Function<String, String> {

	public static final String PARAMS_PREFIX = "params:"; 
	
	public static final String PROPERTIES_PREFIX = "properties:";
	
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
						return StringUtils.join(paramValues, ",");
					} else {
						throw new OneException("Invisible param: " + paramName);
					}
				}					
			}
			throw new OneException("Undefined param: " + paramName);
		} else if (t.startsWith(PROPERTIES_PREFIX)) {
			String propertyName = t.substring(PROPERTIES_PREFIX.length());
			String propertyValue = build.getSpec().getPropertyMap().get(propertyName);
			if (propertyValue != null)
				return propertyValue;
			else
				throw new OneException("Undefined property: " + propertyName);
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

	public static Object installInterceptor(Object object) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(object.getClass());
		enhancer.setCallback(new InterpolatorInterceptor());

		Object enhanced = enhancer.create();

		for (Field field: BeanUtils.findFields(object.getClass())) {
			field.setAccessible(true);
			try {
				field.set(enhanced, field.get(object));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		for (Method getter: BeanUtils.findGetters(object.getClass())) {
			if (getter.getAnnotation(Editable.class) != null) {
				Method setter = BeanUtils.findSetter(getter);
				if (setter != null) {
					try {
						Object propertyValue = getter.invoke(object);
						if (propertyValue != null) {
							if (propertyValue.getClass().getAnnotation(Editable.class) != null) {
								setter.invoke(enhanced, installInterceptor(propertyValue));
							} else if (propertyValue instanceof List) {
								List<Object> enhancedList = new ArrayList<>();
								for (Object element: (List<?>)propertyValue) { 
									if (element.getClass().getAnnotation(Editable.class) != null)
										enhancedList.add(installInterceptor(element));
									else
										enhancedList.add(element);
								}
								setter.invoke(enhanced, enhancedList);
							}
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		return enhanced;
	}
	
	private static class InterpolatorInterceptor implements MethodInterceptor, Serializable {
		
		private static final long serialVersionUID = 1L;
		
		@SuppressWarnings("unchecked")
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			if (BeanUtils.isGetter(method) 
					&& method.getAnnotation(Editable.class) != null 
					&& method.getAnnotation(io.onedev.server.web.editable.annotation.Interpolative.class) != null) {
				Serializable propertyValue = (Serializable) methodProxy.invokeSuper(proxy, args);
				Build build = Build.get();
				try {
					if (build == null) { 
						throw new RuntimeException("No build context");
					} else if (propertyValue instanceof String) {
						return build.interpolate((String) propertyValue);
					} else if (propertyValue instanceof List) {
						List<String> interpolatedList = new ArrayList<>();
						for (String element: (List<String>) propertyValue)  
							interpolatedList.add(build.interpolate(element));
						return interpolatedList;
					}
				} catch (Exception e) {
					String message = String.format("Error doing interpolator intercepting (class: %s, method: %s)", 
							method.getDeclaringClass().getName(), method.getName());
					throw new RuntimeException(message, e);
				}
			} 
			return methodProxy.invokeSuper(proxy, args);
		}		
	}
		
}
