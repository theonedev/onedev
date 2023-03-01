package io.onedev.server.validation;

import javax.validation.ConstraintValidatorContext;

public interface Validatable {
	
	boolean isValid(ConstraintValidatorContext context);
}
