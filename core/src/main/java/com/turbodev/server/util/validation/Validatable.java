package com.turbodev.server.util.validation;

import javax.validation.ConstraintValidatorContext;

public interface Validatable {
	
	boolean isValid(ConstraintValidatorContext context);
}
