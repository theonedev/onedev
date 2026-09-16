package io.onedev.server.validation.validator;

import javax.lang.model.SourceVersion;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.EnvVarName;

public class EnvNameValidator implements ConstraintValidator<EnvVarName, String> {

	private String message;
	
	@Override
	public void initialize(EnvVarName constaintAnnotation) {
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
