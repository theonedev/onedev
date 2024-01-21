package io.onedev.server.validation.validator;

import io.onedev.server.annotation.SubPath;
import org.apache.commons.io.FilenameUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SubPathValidator implements ConstraintValidator<SubPath, String> {

	private String message;
	
	@Override
	public void initialize(SubPath constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;

		if (FilenameUtils.getPrefixLength(value) != 0) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "Absolute path not allowed";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else if (value.contains("..")) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) 
				message = "'..' is disallowed";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
