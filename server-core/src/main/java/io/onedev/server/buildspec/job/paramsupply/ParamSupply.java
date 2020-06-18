package io.onedev.server.buildspec.job.paramsupply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.buildspec.job.paramspec.SecretParam;
import io.onedev.server.model.Build;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ParamSupply implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(ParamSupply.class);
	
	private static final String PARAM_BEAN_PREFIX = "ParamSupplyBean";
	
	private String name;
	
	private boolean secret;
	
	private ValuesProvider valuesProvider = new SpecifiedValues();

	@Editable
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable
	@NotNull
	@Valid
	public ValuesProvider getValuesProvider() {
		return valuesProvider;
	}

	public void setValuesProvider(ValuesProvider valuesProvider) {
		this.valuesProvider = valuesProvider;
	}

	@Editable
	public boolean isSecret() {
		return secret;
	}

	public void setSecret(boolean secret) {
		this.secret = secret;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ParamSupply)) 
			return false;
		if (this == other)
			return true;
		ParamSupply otherParamValue = (ParamSupply) other;
		return new EqualsBuilder()
			.append(name, otherParamValue.name)
			.append(valuesProvider, otherParamValue.valuesProvider)
			.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
			.append(name)
			.append(valuesProvider)
			.toHashCode();
	}		
	
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
					throw new ValidationException("Error validating values of parameter '" 
							+ entry.getKey() + "': " + e.getMessage());
				}
				
				ParamSpec paramSpec = Preconditions.checkNotNull(paramSpecMap.get(entry.getKey()));
				for (List<String> value: entry.getValue()) 
					validateParamValue(paramSpec, entry.getKey(), value);
			}
		}
	}
	
	private static void validateParamValue(ParamSpec paramSpec, String paramName, List<String> paramValue) {
		try {
			paramSpec.convertToObject(paramValue);
		} catch (Exception e) {
			String displayValue;
			if (paramSpec instanceof SecretParam)
				displayValue = SecretInput.MASK;
			else
				displayValue = paramValue.toString();
			if (e.getMessage() == null)
				logger.error("Error validating field value", e);
			throw new ValidationException("Error validating value '" + displayValue + "' of parameter '" 
					+ paramName + "': " + e.getMessage());
		}
	}

	private static void validateParamNames(Collection<String> paramSpecNames, Collection<String> paramNames) {
		for (String paramSpecName: paramSpecNames) {
			if (!paramNames.contains(paramSpecName))
				throw new ValidationException("Missing job parameter: " + paramSpecName);
		}
		for (String paramName: paramNames) {
			if (!paramSpecNames.contains(paramName))
				throw new ValidationException("Unknown job parameter: " + paramName);
		}
	}
	
	public static void validateParamMap(Map<String, ParamSpec> paramSpecMap, Map<String, List<String>> paramMap) {
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
				values = param.getValuesProvider().getValues();
			else
				values = null;
			if (paramMap.put(param.getName(), values) != null)
				throw new ValidationException("Duplicate param: " + param.getName());
		}
		validateParamMatrix(getParamSpecMap(paramSpecs), paramMap);
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineBeanClass(Collection<ParamSpec> paramSpecs) {
		byte[] bytes = SerializationUtils.serialize((Serializable) paramSpecs);
		String className = PARAM_BEAN_PREFIX + "_" + Hex.encodeHexString(bytes);
		
		List<ParamSpec> paramSpecsCopy = new ArrayList<>(paramSpecs);
		for (int i=0; i<paramSpecsCopy.size(); i++) {
			ParamSpec paramSpec = paramSpecsCopy.get(i);
			if (paramSpec instanceof SecretParam) {
				ParamSpec paramSpecClone = (ParamSpec) SerializationUtils.clone(paramSpec);
				String description = paramSpecClone.getDescription();
				if (description == null)
					description = "";
				description += String.format("<div style='margin-top: 12px;'><b>Note:</b> Secret less than %d characters "
						+ "will not be masked in build log</div>", SecretInput.MASK.length());
				paramSpecClone.setDescription(description);
				paramSpecsCopy.set(i, paramSpecClone);
			}
		}
		return (Class<? extends Serializable>) ParamSpec.defineClass(className, "Build Parameters", paramSpecsCopy);
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
						if (paramSpec instanceof SecretParam)
							value = SecretInput.LITERAL_VALUE_PREFIX + value;
						values.add(value);
					}
					paramMap.put(paramSpec.getName(), values);
				}
			}
		}
		return paramMap;
	}

	public static Map<String, List<List<String>>> getParamMatrix(List<ParamSupply> params, @Nullable Build build) {
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
				for (List<String> value: param.getValuesProvider().getValues()) {
					List<String> resolvedValue = new ArrayList<>();
					for (String each: value) 
						resolvedValue.add(SecretInput.LITERAL_VALUE_PREFIX + build.getSecretValue(each));
					resolvedValues.add(resolvedValue);
				}
				paramMatrix.put(param.getName(), resolvedValues);
			} else {
				paramMatrix.put(param.getName(), param.getValuesProvider().getValues());
			}
		}
		return paramMatrix;
	}

}
