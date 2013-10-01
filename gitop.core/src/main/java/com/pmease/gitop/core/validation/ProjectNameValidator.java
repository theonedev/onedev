package com.pmease.gitop.core.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.pmease.gitop.core.Gitop;
import com.pmease.gitop.core.manager.ProjectManager;

public class ProjectNameValidator implements ConstraintValidator<ProjectName, String> {
	
	public void initialize(ProjectName constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;

		constraintContext.disableDefaultConstraintViolation();
		constraintContext.buildConstraintViolationWithTemplate(value + " is a reserved word.").addConstraintViolation();
		return !Gitop.getInstance(ProjectManager.class).getReservedNames().contains(value);
	}
}
