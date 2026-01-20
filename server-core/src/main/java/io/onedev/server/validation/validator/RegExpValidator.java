package io.onedev.server.validation.validator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.RegExp;

public class RegExpValidator implements ConstraintValidator<RegExp, String> {
	
	private String message;
	
	@Override
	public void initialize(RegExp constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		try {
			Pattern.compile(value);
		} catch (Exception e) {
			String message = this.message;
			if (message.length() == 0) {
				message = "Not a valid regular expression";
			}
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
