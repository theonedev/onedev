package io.onedev.server.util.validation;

import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.Pattern;

public class PatternValidator implements ConstraintValidator<Pattern, String> {

	@Override
	public void initialize(Pattern constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				java.util.regex.Pattern.compile(value);
				return true;
			} catch (PatternSyntaxException e) {
				constraintContext.disableDefaultConstraintViolation();
				String message = "Not a valid Java pattern";
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
