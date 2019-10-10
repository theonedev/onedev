package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.interpolative.Segment;
import io.onedev.server.util.validation.annotation.CommitHash;

public class CommitHashValidator implements ConstraintValidator<CommitHash, String> {

	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(CommitHash constaintAnnotation) {
		interpolative = constaintAnnotation.interpolative();
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null || interpolative && !Interpolated.get() && !Interpolative.fromString(value).getSegments(Segment.Type.VARIABLE).isEmpty())
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
