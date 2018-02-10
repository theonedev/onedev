package com.turbodev.server.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.turbodev.server.util.validation.annotation.UserName;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	private static Pattern pattern = Pattern.compile("^[\\w-\\.]+$");
	
	public void initialize(UserName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else if (!pattern.matcher(value).find()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = "Only alphanumeric, underscore, dash, and dot are accepted";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.equals("new")) {
			constraintContext.disableDefaultConstraintViolation();
			String message = "'new' is reserved and can not be used as user name";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
