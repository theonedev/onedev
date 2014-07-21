package com.pmease.gitplex.core.validation;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.pmease.commons.loader.AppLoader;

public class RepositoryNameValidator implements ConstraintValidator<RepositoryName, String> {

	private static Set<String> reservedNames;
	
	public void initialize(RepositoryName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;

		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(value + " is a reserved word.").addConstraintViolation();
		
		return !getReservedNames().contains(value);
	}
	
	public static synchronized Set<String> getReservedNames() {
		if (reservedNames == null) {
			reservedNames = new HashSet<>();
	        for (RepositoryNameReservation each : AppLoader.getExtensions(RepositoryNameReservation.class)) {
	        	reservedNames.addAll(each.getReserved());
	        }
		}
		return reservedNames;
	}
}
