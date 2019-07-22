package io.onedev.server.util.validation;

import javax.validation.ConstraintValidatorContext;

public interface Validatable {
	
	boolean isValid(ConstraintValidatorContext context);
}
