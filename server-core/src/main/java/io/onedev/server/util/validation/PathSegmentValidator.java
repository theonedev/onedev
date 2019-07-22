package io.onedev.server.util.validation;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.PathSegment;

public class PathSegmentValidator implements ConstraintValidator<PathSegment, String> {

	@Override
	public void initialize(PathSegment constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;

		String errorMessage = null;
		if (value.contains("/") || value.contains("\\")) {
			errorMessage = "Slash and back slash characters are not allowed";
		} else {
			try {
				Paths.get(value);
			} catch (Exception e) {
				errorMessage = e.getMessage();
			}
		}
		
		if (errorMessage != null) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
			return false;
		}
		
		return true;
	}
}
