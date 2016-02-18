package com.pmease.gitplex.core.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.pmease.gitplex.core.model.Team;

public class TeamNameValidator implements ConstraintValidator<TeamName, String> {

	public void initialize(TeamName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) return true;

		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(value + " is a reserved word.")
				.addConstraintViolation();
		return !value.equalsIgnoreCase(Team.ANONYMOUS) && !value.equalsIgnoreCase(Team.LOGGEDIN)
				&& !value.equalsIgnoreCase(Team.OWNERS);
	}
}
