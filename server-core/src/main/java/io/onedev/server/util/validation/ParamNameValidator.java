package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Build;
import io.onedev.server.util.validation.annotation.ParamName;

public class ParamNameValidator implements ConstraintValidator<ParamName, String> {

	private static final Pattern PATTERN = Pattern.compile("\\w([\\w-\\.]*\\w)?");
	
	private String message;
	
	@Override
	public void initialize(ParamName constaintAnnotation) {
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
						+ "Only alphanumeric, underscore, dash, and dot are allowed in the middle.";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (Build.ALL_FIELDS.contains(value)) {
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
