package io.onedev.server.util.validation;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.PathSegment;

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
		if (value.contains("/") || value.contains("\\")) {
			valid = false;
			if (message.length() == 0)
				message = "Slash and back slash characters are not allowed";
		} else {
			try {
				Paths.get(value);
			} catch (Exception e) {
				valid = false;
				if (message.length() == 0)
					message = e.getMessage();
			}
		}
		
		if (!valid) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
		
		return true;
	}
}
