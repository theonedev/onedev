package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.ClassValidating;

public class ClassValidatingValidator implements ConstraintValidator<ClassValidating, Validatable> {

	@Override
	public void initialize(ClassValidating constraintAnnotation) {
	}

	@Override
	public boolean isValid(Validatable value, ConstraintValidatorContext constraintValidatorContext) {
		return value.isValid(constraintValidatorContext);
	}
	
}