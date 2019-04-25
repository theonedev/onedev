package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.validation.annotation.InputName;

public class InputNameValidator implements ConstraintValidator<InputName, String> {

	@Override
	public void initialize(InputName constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			String errorMessage = OneContext.get().getInputContext().validateName(value);			
			if (errorMessage != null) {
				constraintContext.disableDefaultConstraintViolation();
				constraintContext.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
				return false;
			} else {
				return true;
			}
		} 
	}
	
}
