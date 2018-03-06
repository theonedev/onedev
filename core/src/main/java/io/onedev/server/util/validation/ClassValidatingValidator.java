package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.ClassValidating;

public class ClassValidatingValidator implements ConstraintValidator<ClassValidating, Validatable> {

	public void initialize(ClassValidating constraintAnnotation) {
	}

	public boolean isValid(Validatable value, ConstraintValidatorContext constraintValidatorContext) {
		return value.isValid(constraintValidatorContext);
	}
	
}