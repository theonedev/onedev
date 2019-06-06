package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ValidationException;

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
			try {
				OneContext.get().getInputContext().validateName(value);			
				return true;
			} catch (ValidationException e) {
				constraintContext.disableDefaultConstraintViolation();
				constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
				return false;
			}
		} 
	}
	
}
