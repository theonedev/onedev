package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Issue;
import io.onedev.server.util.validation.annotation.FieldName;

public class FieldNameValidator implements ConstraintValidator<FieldName, String> {

	private String message;
	
	@Override
	public void initialize(FieldName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (Issue.ALL_FIELDS.contains(value)) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "' is reserved";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
