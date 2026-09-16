package io.onedev.server.validation;

import jakarta.validation.ConstraintValidatorContext;

public interface Validatable {
	
	boolean isValid(ConstraintValidatorContext context);
}
