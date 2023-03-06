package io.onedev.server.buildspec.param;

import com.google.common.base.Preconditions;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.buildspec.param.spec.SecretParam;
import io.onedev.server.buildspec.param.supply.ParamSupply;
import io.onedev.server.buildspec.param.supply.SpecifiedValues;
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

import javax.annotation.Nullable;
import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.*;

public class ParamUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ParamSupply.class);
	
	private static final String PARAM_BEAN_PREFIX = "ParamSupplyBean";
	
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
	
	public static void validateParamMatrix(Map<String, ParamSpec> paramSpecMap, Map<String, List<List<String>>> paramMatrix) {
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
					paramName, displayValue, e.getMessage());
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
			ParamSpec paramSpec = Preconditions.checkNotNull(paramSpecMap.get(entry.getKey()));
			validateParamValue(paramSpec, entry.getKey(), entry.getValue());
		}
	}
	
	public static Map<String, ParamSpec> getParamSpecMap(List<ParamSpec> paramSpecs) {
		Map<String, ParamSpec> paramSpecMap = new LinkedHashMap<>();
		for (ParamSpec paramSpec: paramSpecs)
			paramSpecMap.put(paramSpec.getName(), paramSpec);
		return paramSpecMap;
	}
	
	public static void validateParams(List<ParamSpec> paramSpecs, List<ParamSupply> params) {
		Map<String, List<List<String>>> paramMap = new HashMap<>();
		for (ParamSupply param: params) {
			List<List<String>> values;
			if (param.getValuesProvider() instanceof SpecifiedValues)
				values = param.getValuesProvider().getValues(null, null);
			else
				values = null;
			if (paramMap.put(param.getName(), values) != null)
				throw new ValidationException("Duplicate param (" + param.getName() + ")");
		}
		validateParamMatrix(getParamSpecMap(paramSpecs), paramMap);
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineBeanClass(Collection<ParamSpec> paramSpecs) {
		byte[] bytes = SerializationUtils.serialize((Serializable) paramSpecs);
		String className = PARAM_BEAN_PREFIX + "_" + Hex.encodeHexString(bytes);
		
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
		if (className.startsWith(PARAM_BEAN_PREFIX)) {
			byte[] bytes;
			try {
				bytes = Hex.decodeHex(className.substring(PARAM_BEAN_PREFIX.length()+1).toCharArray());
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

	public static Map<String, List<List<String>>> getParamMatrix(@Nullable Build build, 
			@Nullable ParamCombination paramCombination, List<ParamSupply> params) {
		Map<String, List<List<String>>> paramMatrix = new LinkedHashMap<>();
		for (ParamSupply param: params) {
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
				paramMatrix.put(param.getName(), resolvedValues);
			} else {
				paramMatrix.put(param.getName(), param.getValuesProvider().getValues(build, paramCombination));
			}
		}
		return paramMatrix;
	}

}
