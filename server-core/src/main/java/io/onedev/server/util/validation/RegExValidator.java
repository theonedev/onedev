package io.onedev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.RegEx;

public class RegExValidator implements ConstraintValidator<RegEx, String> {

	private Pattern pattern;
	
	private String message;
	
	@Override
	public void initialize(RegEx constaintAnnotation) {
		pattern = Pattern.compile(constaintAnnotation.pattern());
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		if (pattern.matcher(value).matches()) {
			return true;
		} else {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
	
}
