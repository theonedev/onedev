package io.onedev.server.util.validation;

import javax.lang.model.SourceVersion;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.query.BuildQueryConstants;
import io.onedev.server.util.validation.annotation.ParamName;

public class ParamNameValidator implements ConstraintValidator<ParamName, String> {

	private String message;
	
	@Override
	public void initialize(ParamName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		String message = this.message;
		if (!SourceVersion.isIdentifier(value) || value.contains("$")) { 
			constraintContext.disableDefaultConstraintViolation();
			if (message.length() == 0) {
				message = "param name should start with letter and can only consist of "
						+ "alphanumeric and underscore characters";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (BuildQueryConstants.ALL_FIELDS.contains(value)) {
			constraintContext.disableDefaultConstraintViolation();
			if (message.length() == 0)
				message = "'" + value + "' is reserved";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
