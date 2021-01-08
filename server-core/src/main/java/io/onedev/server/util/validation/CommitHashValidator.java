package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.util.validation.annotation.CommitHash;

public class CommitHashValidator implements ConstraintValidator<CommitHash, String> {

	private String message;
	
	@Override
	public void initialize(CommitHash constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		if (!ObjectId.isId(value)) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
