package com.pmease.gitop.model.validation;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.pmease.commons.loader.AppLoader;

public class ProjectNameValidator implements ConstraintValidator<ProjectName, String> {

	private static Set<String> reservedNames;
	
	public void initialize(ProjectName constaintAnnotation) {
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
	        for (ProjectNameReservation each : AppLoader.getExtensions(ProjectNameReservation.class)) {
	        	reservedNames.addAll(each.getReserved());
	        }
		}
		return reservedNames;
	}
}
