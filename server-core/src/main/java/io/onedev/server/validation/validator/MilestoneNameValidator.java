package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.model.Milestone;
import io.onedev.server.annotation.MilestoneName;

public class MilestoneNameValidator implements ConstraintValidator<MilestoneName, String> {

	private String message;
	
	@Override
	public void initialize(MilestoneName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (value.equals(Milestone.NONE)) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "' is a reserved name";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}	
	}
	
}
