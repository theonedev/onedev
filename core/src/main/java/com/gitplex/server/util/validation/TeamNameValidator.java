package com.gitplex.server.util.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.util.validation.annotation.TeamName;

public class TeamNameValidator implements ConstraintValidator<TeamName, String> {

	private static Set<String> reservedNames;
	
	private static Pattern pattern = Pattern.compile("^[\\w-\\.]+$");
	
	public void initialize(TeamName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else if (!pattern.matcher(value).find()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = "Only alphanumeric, underscore, dash, and dot are accepted";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (getReservedNames().contains(value)) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(value + " is a reserved word").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
	public static synchronized Set<String> getReservedNames() {
		if (reservedNames == null) {
			reservedNames = new HashSet<>();
	        for (TeamNameReservation each : AppLoader.getExtensions(TeamNameReservation.class)) {
	        	reservedNames.addAll(each.getReserved());
	        }
		}
		return reservedNames;
	}
	
}
