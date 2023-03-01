package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.SafePath;

public class SafePathValidator implements ConstraintValidator<SafePath, String> {

	private String message;
	
	@Override
	public void initialize(SafePath constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		if (value.contains("..")) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) 
				message = "'..' is disallowed";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
