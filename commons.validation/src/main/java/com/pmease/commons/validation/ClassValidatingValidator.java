package com.pmease.commons.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

public class ClassValidatingValidator implements ConstraintValidator<ClassValidating, Validatable> {

	public void initialize(ClassValidating constraintAnnotation) {
	}

	public boolean isValid(Validatable value, ConstraintValidatorContext constraintValidatorContext) {
		ConstraintValidatorContextImpl impl = (ConstraintValidatorContextImpl)constraintValidatorContext;
		impl.disableDefaultConstraintViolation();
		value.validate(constraintValidatorContext);
		return impl.getMessageAndPathList().isEmpty();
	}
	
}