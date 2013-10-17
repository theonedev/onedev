package com.pmease.commons.validation;

import javax.validation.ConstraintValidatorContext;

public interface Validatable {
	void validate(ConstraintValidatorContext constraintValidatorContext);
}
