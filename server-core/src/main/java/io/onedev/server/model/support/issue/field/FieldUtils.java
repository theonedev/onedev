package io.onedev.server.model.support.issue.field;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.model.support.issue.field.spec.FieldSpec;
import io.onedev.server.model.support.issue.field.spec.SecretField;
import io.onedev.server.model.support.issue.field.supply.FieldSupply;
import io.onedev.server.model.support.issue.field.supply.SpecifiedValue;

public class FieldUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(FieldSupply.class);
	
	private static void validateFieldValue(FieldSpec fieldSpec, String fieldName, List<String> fieldValue) {
		try {
			fieldSpec.convertToObject(fieldValue);
		} catch (Exception e) {
			String displayValue;
			if (fieldSpec instanceof SecretField)
				displayValue = SecretInput.MASK;
			else
				displayValue = fieldValue.toString();
			if (e.getMessage() == null)
				logger.error("Error validating field value", e);
			throw new ValidationException("Error validating value '" + displayValue + "' of field '" 
					+ fieldName + "': " + e.getMessage());
		}
	}

	private static void validateFieldNames(Collection<String> fieldSpecNames, Collection<String> fieldNames) {
		for (String fieldSpecName: fieldSpecNames) {
			if (!fieldNames.contains(fieldSpecName))
				throw new ValidationException("Missing issue field: " + fieldSpecName);
		}
		for (String fieldName: fieldNames) {
			if (!fieldSpecNames.contains(fieldName))
				throw new ValidationException("Unknown issue field: " + fieldName);
		}
	}
	
	public static void validateFieldMap(Map<String, FieldSpec> fieldSpecMap, Map<String, List<String>> fieldMap) {
		validateFieldNames(fieldSpecMap.keySet(), fieldMap.keySet());
		for (Map.Entry<String, List<String>> entry: fieldMap.entrySet()) {
			if (entry.getValue() != null) {
				FieldSpec fieldSpec = Preconditions.checkNotNull(fieldSpecMap.get(entry.getKey()));
				validateFieldValue(fieldSpec, entry.getKey(), entry.getValue());
			}
		}
	}
	
	public static void validateFields(Map<String, FieldSpec> fieldSpecs, List<FieldSupply> fields) {
		Map<String, List<String>> fieldMap = new HashMap<>();
		for (FieldSupply field: fields) {
			List<String> values;
			if (field.getValueProvider() instanceof SpecifiedValue)
				values = field.getValueProvider().getValue();
			else
				values = null;
			if (fieldMap.put(field.getName(), values) != null)
				throw new ValidationException("Duplicate field: " + field.getName());
		}
		validateFieldMap(fieldSpecs, fieldMap);
	}

}
