package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.validation.Validatable;

public class ClassValidatingValidator implements ConstraintValidator<ClassValidating, Validatable> {

	@Override
	public void initialize(ClassValidating constraintAnnotation) {
	}

	@Override
	public boolean isValid(Validatable value, ConstraintValidatorContext constraintValidatorContext) {
		return value.isValid(constraintValidatorContext);
	}
	
}