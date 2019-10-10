package io.onedev.server.util.validation;

import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.JavaPattern;

public class JavaPatternValidator implements ConstraintValidator<JavaPattern, String> {

	private String message;
	
	@Override
	public void initialize(JavaPattern constaintAnnotation) {
		message = constaintAnnotation.message();
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
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
