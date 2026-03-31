package io.onedev.server.validation.validator;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.PathSegment;

public class PathSegmentValidator implements ConstraintValidator<PathSegment, String> {

	private String message;
	
	@Override
	public void initialize(PathSegment constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;

		boolean valid = true;
		String message = this.message;

		String checkMessage = checkPathSegment(value);
		if (checkMessage != null) {
			valid = false;
			if (message.length() == 0)
				message = checkMessage;
		}
		
		if (!valid) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
		
		return true;
	}

	public static String checkPathSegment(String value) {
		if (value.contains(".."))
			return "'..' is not allowed";
		else if (value.contains("/") || value.contains("\\"))
			return "Slash and back slash characters are not allowed";

		try {
			Paths.get(value);
		} catch (Exception e) {
			return e.getMessage();
		}
		return null;
	}

}
