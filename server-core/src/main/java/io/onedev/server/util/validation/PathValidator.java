package io.onedev.server.util.validation;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.Path;

public class PathValidator implements ConstraintValidator<Path, String> {

	private String message;
	
	@Override
	public void initialize(Path constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		try {
			Paths.get(value);
			return true;
		} catch (Exception e) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
}
