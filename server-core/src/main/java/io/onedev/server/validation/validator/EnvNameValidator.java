package io.onedev.server.validation.validator;

import javax.lang.model.SourceVersion;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.EnvName;

public class EnvNameValidator implements ConstraintValidator<EnvName, String> {

	private String message;
	
	@Override
	public void initialize(EnvName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!SourceVersion.isIdentifier(value) || value.contains("$")) {  
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		} 
	}
	
}
