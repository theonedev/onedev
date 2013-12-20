package com.pmease.commons.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;

public class ClassValidatingValidator implements ConstraintValidator<ClassValidating, Validatable> {

	public void initialize(ClassValidating constraintAnnotation) {
	}

	public boolean isValid(Validatable value, ConstraintValidatorContext constraintValidatorContext) {
		ConstraintValidatorContextImpl impl = (ConstraintValidatorContextImpl)constraintValidatorContext;
		value.validate(constraintValidatorContext);
		
		// There are new violations besides the default one, so we proceed to remove the default one. 
		// We did not disable the default violation before calling validate method as otherwise calling 
		// to getMessageAndPathList() will complain about no default violations if no validation errors 
		// is added to it in the validate method. 
		if (impl.getMessageAndPathList().size() > 1) {
			impl.disableDefaultConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}