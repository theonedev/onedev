package io.onedev.server.util.validation;

import javax.lang.model.SourceVersion;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.VariableName;

public class VariableNameValidator implements ConstraintValidator<VariableName, String> {

	private String message;
	
	@Override
	public void initialize(VariableName constaintAnnotation) {
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
