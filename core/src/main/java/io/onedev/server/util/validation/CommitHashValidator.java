package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.git.GitUtils;
import io.onedev.server.util.validation.annotation.CommitHash;

public class CommitHashValidator implements ConstraintValidator<CommitHash, String> {

	@Override
	public void initialize(CommitHash constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else if (!GitUtils.isHash(value)) {
			constraintContext.disableDefaultConstraintViolation();
			String message = "'" + value + "' is not a valid commit hash";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
