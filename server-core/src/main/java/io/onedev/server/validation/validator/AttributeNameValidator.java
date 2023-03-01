package io.onedev.server.validation.validator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Agent;
import io.onedev.server.annotation.AttributeName;

public class AttributeNameValidator implements ConstraintValidator<AttributeName, String> {

	public static final Pattern PATTERN = Pattern.compile("\\w([\\w-\\.\\s]*\\w)?");

	private String message;
	
	@Override
	public void initialize(AttributeName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		String message = this.message;
		if (!PATTERN.matcher(value).matches()) {
			if (message.length() == 0) {
				message = "Should start and end with alphanumeric or underscore. "
						+ "Only alphanumeric, underscore, dash, space and dot are allowed in the middle.";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (Agent.ALL_FIELDS.contains(value)) {
			constraintContext.disableDefaultConstraintViolation();
			if (message.length() == 0)
				message = "'" + value + "' is reserved";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
