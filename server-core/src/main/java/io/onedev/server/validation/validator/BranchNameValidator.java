package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.eclipse.jgit.lib.Repository;

import io.onedev.server.annotation.BranchName;
import io.onedev.server.git.GitUtils;

public class BranchNameValidator implements ConstraintValidator<BranchName, String> {
	
	private String message;
	
	@Override
	public void initialize(BranchName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!Repository.isValidRefName(GitUtils.branch2ref(value))) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				message = "Invalid git branch name";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
