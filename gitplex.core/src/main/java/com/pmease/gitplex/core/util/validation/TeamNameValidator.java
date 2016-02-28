package com.pmease.gitplex.core.util.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class TeamNameValidator implements ConstraintValidator<TeamName, String> {

	private static Pattern pattern = Pattern.compile("^[\\w-\\.]+$");
	
	public void initialize(TeamName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else if (!pattern.matcher(value).find()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = "Only alphanumeric, underscore, dash, and dot are accepted.";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
