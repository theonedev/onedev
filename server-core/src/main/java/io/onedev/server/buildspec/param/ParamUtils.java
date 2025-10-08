package io.onedev.server.buildspec.param;

import com.google.common.base.Preconditions;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.param.instance.*;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.spec.SecretParam;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.HtmlUtils;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jspecify.annotations.Nullable;
import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ParamUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ParamInstances.class);
	
	private static final String PARAM_BEAN_CLASS_NAME_PREFIX = "BuildParamBean";
	
	public static void validateParamValues(List<List<String>> values) {
		if (values.isEmpty())
			throw new ValidationException("At least one value needs to be specified");
		Set<List<String>> encountered = new HashSet<>();
		for (List<String> value: values) {
			if (encountered.contains(value)) 
				throw new ValidationException("Duplicate values not allowed");
			else 
				encountered.add(value);
		}
	}
	
	public static void validateParamMatrix(List<ParamSpec> paramSpecs, Map<String, List<List<String>>> paramMatrix) {
		Map<String, ParamSpec> paramSpecMap = ParamUtils.getParamSpecMap(paramSpecs);
		validateParamNames(paramSpecMap.keySet(), paramMatrix.keySet());
		for (Map.Entry<String, List<List<String>>> entry: paramMatrix.entrySet()) {
			if (entry.getValue() != null) {
				try {
					validateParamValues(entry.getValue());
				} catch (ValidationException e) {
					String errorMessage = String.format("Error validating param values (param: %s, error message: %s)", 
							entry.getKey(), e.getMessage());
					throw new ValidationException(errorMessage);
				}
				
				ParamSpec paramSpec = Preconditions.checkNotNull(paramSpecMap.get(entry.getKey()));
				for (List<String> value: entry.getValue()) 
					validateParamValue(paramSpec, entry.getKey(), value);
			}
		}
	}
	
	private static void validateParamValue(ParamSpec paramSpec, String paramName, List<String> paramValue) {
		try {
			Object object = paramSpec.convertToObject(paramValue);
			if (paramSpec instanceof SecretParam && object != null 
					&& !((String)object).startsWith(SecretInput.LITERAL_VALUE_PREFIX)) {
				if (!Project.get().getHierarchyJobSecrets().stream()
						.anyMatch(it->it.getName().equals(object))) {
					throw new ValidationException("Secret not found");
				}
			}
		} catch (Exception e) {
			if (e.getMessage() == null)
				logger.error("Error validating field value", e);
			
			List<String> displayValue;
			if (paramSpec instanceof SecretParam) {
				displayValue = new ArrayList<>();
				for (String each: paramValue) {
					if (each.startsWith(SecretInput.LITERAL_VALUE_PREFIX))
						displayValue.add(SecretInput.MASK);
					else
						displayValue.add(each);
				}
			} else {
				displayValue = paramValue;
			}
			String errorMessage = String.format("Error validating param value (param: %s, value: %s, error message: %s", 
					paramName, displayValue, e.getMessage() != null? e.getMessage(): e.getClass());
			throw new ValidationException(errorMessage);
		}
	}

	private static void validateParamNames(Collection<String> paramSpecNames, Collection<String> paramNames) {
		for (String paramSpecName: paramSpecNames) {
			if (!paramNames.contains(paramSpecName))
				throw new ValidationException("Missing job parameter (" + paramSpecName + ")");
		}
		for (String paramName: paramNames) {
			if (!paramSpecNames.contains(paramName))
				throw new ValidationException("Unknown job parameter (" + paramName + ")");
		}
	}
	
	public static void validateParamMap(List<ParamSpec> paramSpecs, Map<String, List<String>> paramMap) {
		Map<String, ParamSpec> paramSpecMap = ParamUtils.getParamSpecMap(paramSpecs);
		validateParamNames(paramSpecMap.keySet(), paramMap.keySet());
		for (Map.Entry<String, List<String>> entry: paramMap.entrySet()) {
			if (entry.getValue() != null) {
				ParamSpec paramSpec = Preconditions.checkNotNull(paramSpecMap.get(entry.getKey()));
				validateParamValue(paramSpec, entry.getKey(), entry.getValue());
			}
		}
	}
	
	public static Map<String, ParamSpec> getParamSpecMap(List<ParamSpec> paramSpecs) {
		Map<String, ParamSpec> paramSpecMap = new LinkedHashMap<>();
		for (ParamSpec paramSpec: paramSpecs)
			paramSpecMap.put(paramSpec.getName(), paramSpec);
		return paramSpecMap;
	}

	public static void validateParamMatrix(List<ParamSpec> paramSpecs, List<ParamInstances> paramMatrix) {
		Map<String, List<List<String>>> evaledParamMatrix = new HashMap<>();
		for (ParamInstances param: paramMatrix) {
			List<List<String>> values;
			if (param.getValuesProvider() instanceof SpecifiedValues)
				values = param.getValuesProvider().getValues(null, null);
			else
				values = null;
			if (evaledParamMatrix.put(param.getName(), values) != null)
				throw new ValidationException("Duplicate param (" + param.getName() + ")");
		}
		validateParamMatrix(paramSpecs, evaledParamMatrix);
	}

	public static void validateParamMap(List<ParamSpec> paramSpecs, List<ParamInstance> paramMap) {
		Map<String, List<String>> evaledParamMap = new HashMap<>();
		for (ParamInstance param: paramMap) {
			List<String> value;
			if (param.getValueProvider() instanceof SpecifiedValue)
				value = param.getValueProvider().getValue(null, null);
			else
				value = null;
			if (evaledParamMap.put(param.getName(), value) != null)
				throw new ValidationException("Duplicate param (" + param.getName() + ")");
		}
		validateParamMap(paramSpecs, evaledParamMap);
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineBeanClass(Collection<ParamSpec> paramSpecs) {
		byte[] bytes = SerializationUtils.serialize((Serializable) paramSpecs);
		String className = PARAM_BEAN_CLASS_NAME_PREFIX + "_" + Hex.encodeHexString(bytes);
		
		List<ParamSpec> paramSpecsCopy = new ArrayList<>();
		for (var paramSpec: paramSpecs) {
			ParamSpec paramSpecClone = (ParamSpec) SerializationUtils.clone(paramSpec);
			var description = paramSpecClone.getDescription();
			if (description != null) 
				description = HtmlUtils.sanitize(description);
			if (paramSpec instanceof SecretParam) {
				if (description == null)
					description = "";
				description += String.format("<div style='margin-top: 12px;'><b>Note:</b> Secret less than %d characters "
						+ "will not be masked in build log</div>", SecretInput.MASK.length());
			}
			paramSpecClone.setDescription(description);
			paramSpecsCopy.add(paramSpecClone);
		}
		return (Class<? extends Serializable>) ParamSpec.defineClass(className, "Parameters", paramSpecsCopy);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public static Class<? extends Serializable> loadBeanClass(String className) {
		if (className.startsWith(PARAM_BEAN_CLASS_NAME_PREFIX)) {
			byte[] bytes;
			try {
				bytes = Hex.decodeHex(className.substring(PARAM_BEAN_CLASS_NAME_PREFIX.length()+1).toCharArray());
			} catch (DecoderException e) {
				throw new RuntimeException(e);
			}
			List<ParamSpec> paramSpecs = (List<ParamSpec>) SerializationUtils.deserialize(bytes);
			return defineBeanClass(paramSpecs);
		} else {
			return null;
		}
	}

	public static Map<String, List<String>> getParamMap(Job job, Object paramBean, Collection<String> paramNames) {
		Map<String, List<String>> paramMap = new HashMap<>();
		BeanDescriptor descriptor = new BeanDescriptor(paramBean.getClass());
		for (List<PropertyDescriptor> groupProperties: descriptor.getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (paramNames.contains(property.getDisplayName()))	{
					Object typedValue = property.getPropertyValue(paramBean);
					ParamSpec paramSpec = Preconditions.checkNotNull(job.getParamSpecMap().get(property.getDisplayName()));
					List<String> values = new ArrayList<>();
					for (String value: paramSpec.convertToStrings(typedValue)) {
						if (paramSpec instanceof SecretParam && !value.startsWith(SecretInput.LITERAL_VALUE_PREFIX))
							value = SecretInput.LITERAL_VALUE_PREFIX + value;
						values.add(value);
					}
					paramMap.put(paramSpec.getName(), values);
				}
			}
		}
		return paramMap;
	}

	public static List<Map<String, List<String>>> resolveParams(
			@Nullable Build build, @Nullable ParamCombination paramCombination,
			List<ParamInstances> paramMatrix, List<? extends ParamMap> excludeParamMaps) {
		var paramMaps = getParamMaps(evalParamMatrix(build, paramCombination, paramMatrix));
		var secretParamNames = paramMatrix.stream()
				.filter(ParamInstances::isSecret)
				.map(ParamInstances::getName)
				.collect(Collectors.toSet());
		for (var excludeParamMap: excludeParamMaps) {
			var evaledExcludeParamMap = evalParamMap(build, paramCombination, excludeParamMap.getParams());
			for (var it = paramMaps.iterator(); it.hasNext();) {
				if (isCoveredBy(it.next(), evaledExcludeParamMap, secretParamNames))				
					it.remove();
			}
		}
		return paramMaps;
	}
	
	public static Map<String, List<List<String>>> evalParamMatrix(@Nullable Build build,
																  @Nullable ParamCombination paramCombination,
																  List<ParamInstances> paramMatrix) {
		Map<String, List<List<String>>> evaledParamMatrix = new LinkedHashMap<>();
		for (ParamInstances param: paramMatrix) {
			/*
			 * Resolve secret value with current build context as otherwise we may not be authorized to 
			 * access the secret value. This is possible for instance if a pull request triggers a job, 
			 * and then post-action of the job triggers another job with a parameter taking value of 
			 * a secret accessible by the pull request   
			 */
			if (param.isSecret() && build != null) { 
				List<List<String>> resolvedValues = new ArrayList<>();
				for (List<String> value: param.getValuesProvider().getValues(build, paramCombination)) {
					List<String> resolvedValue = new ArrayList<>();
					for (String each: value) 
						resolvedValue.add(SecretInput.LITERAL_VALUE_PREFIX + build.getJobAuthorizationContext().getSecretValue(each));
					resolvedValues.add(resolvedValue);
				}
				evaledParamMatrix.put(param.getName(), resolvedValues);
			} else {
				evaledParamMatrix.put(param.getName(), param.getValuesProvider().getValues(build, paramCombination));
			}
		}
		return evaledParamMatrix;
	}

	public static Map<String, List<String>> evalParamMap(@Nullable Build build,
															   @Nullable ParamCombination paramCombination,
															   List<ParamInstance> paramMap) {
		Map<String, List<String>> evaledParamMap = new LinkedHashMap<>();
		for (ParamInstance param: paramMap) {
			/*
			 * Resolve secret value with current build context as otherwise we may not be authorized to
			 * access the secret value. This is possible for instance if a pull request triggers a job,
			 * and then post-action of the job triggers another job with a parameter taking value of
			 * a secret accessible by the pull request
			 */
			if (param.isSecret() && build != null) {
				List<String> resolvedValue = new ArrayList<>();
				for (String value: param.getValueProvider().getValue(build, paramCombination)) 
					resolvedValue.add(SecretInput.LITERAL_VALUE_PREFIX + build.getJobAuthorizationContext().getSecretValue(value));
				evaledParamMap.put(param.getName(), resolvedValue);
			} else {
				evaledParamMap.put(param.getName(), param.getValueProvider().getValue(build, paramCombination));
			}
		}
		return evaledParamMap;
	}
	
	private static List<Map<String, List<String>>> getParamMaps(Map<String, List<List<String>>> paramMatrix) {
		return getParamMaps(paramMatrix, new LinkedHashMap<>());
	}

	private static List<Map<String, List<String>>> getParamMaps(Map<String, List<List<String>>> paramMatrix,
																Map<String, List<String>> paramMap) {
		List<Map<String, List<String>>> paramMaps = new ArrayList<>();
		if (!paramMatrix.isEmpty()) {
			Map.Entry<String, List<List<String>>> entry = paramMatrix.entrySet().iterator().next();
			for (var value: entry.getValue()) {
				var paramMapCopy = new LinkedHashMap<>(paramMap);
				paramMapCopy.put(entry.getKey(), value);
				var paramMatrixCopy = new LinkedHashMap<>(paramMatrix);
				paramMatrixCopy.remove(entry.getKey());
				paramMaps.addAll(getParamMaps(paramMatrixCopy, paramMapCopy));
			}
		} else {
			paramMaps.add(paramMap);
		}
		return paramMaps;
	}

	public static boolean isCoveredBy(Map<String, List<String>> paramMap,
									  Map<String, List<String>> coveringParamMap,
									  Collection<String> secretParamNames) {
		for (var entry: coveringParamMap.entrySet()) {
			if (!secretParamNames.contains(entry.getKey()) && !entry.getValue().isEmpty()) {
				var paramValue = paramMap.get(entry.getKey());
				if (paramValue == null 
						|| paramValue.isEmpty() 
						|| !new HashSet<>(paramValue).equals(new HashSet<>(entry.getValue()))) {
					return false;
				}
			}
		}
		return true;
	} 
	
}
