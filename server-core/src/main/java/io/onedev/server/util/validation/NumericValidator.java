package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.web.editable.annotation.Numeric;

public class NumericValidator implements ConstraintValidator<Numeric, String> {

	private String message;
	
	@Override
	public void initialize(Numeric constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		try {
			Long.parseLong(value);
		} catch (NumberFormatException e) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
