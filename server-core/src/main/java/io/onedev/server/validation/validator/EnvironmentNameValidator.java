package io.onedev.server.validation.validator;

import javax.lang.model.SourceVersion;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.EnvironmentName;

public class EnvironmentNameValidator implements ConstraintValidator<EnvironmentName, String> {

	private String message;
	
	@Override
	public void initialize(EnvironmentName constaintAnnotation) {
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
