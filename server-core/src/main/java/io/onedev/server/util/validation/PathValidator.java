package io.onedev.server.util.validation;

import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.util.validation.annotation.Path;

public class PathValidator implements ConstraintValidator<Path, String> {

	@Override
	public void initialize(Path constaintAnnotation) {
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
			constraintContext.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
			return false;
		}
	}
}
