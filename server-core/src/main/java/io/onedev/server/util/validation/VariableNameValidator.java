package io.onedev.server.util.validation;

import javax.lang.model.SourceVersion;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.VariableName;

public class VariableNameValidator implements ConstraintValidator<VariableName, String> {

	@Override
	public void initialize(VariableName constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else if (!SourceVersion.isIdentifier(value) || value.contains("$")) {  
			constraintContext.disableDefaultConstraintViolation();
			String errorMessage = "name should start with letter and can only consist of "
					+ "alphanumeric and underscore characters";
			constraintContext.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		} else {
			return true;
		} 
	}
	
}
