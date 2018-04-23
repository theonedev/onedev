package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.OneContext;
import io.onedev.server.util.validation.annotation.UserName;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	private static final NameValidator NAME_VALIDATOR = new NameValidator();
	
	public void initialize(UserName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		boolean isValid = NAME_VALIDATOR.isValid(value, constraintContext);
		if (isValid) {
			if (OneContext.get().getInputContext().isReservedName(value)) {
				constraintContext.disableDefaultConstraintViolation();
				String message = "'" + value + "' is a reserved name";
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
}
