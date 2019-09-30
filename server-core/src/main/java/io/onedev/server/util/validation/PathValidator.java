package io.onedev.server.util.validation;

import java.nio.file.Paths;
import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.validation.annotation.Path;

public class PathValidator implements ConstraintValidator<Path, String> {

	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(Path constaintAnnotation) {
		interpolative = constaintAnnotation.interpolative();
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		if (interpolative && !Interpolated.get()) try {
			value = StringUtils.unescape(Interpolative.fromString(value).interpolateWith(new Function<String, String>() {

				@Override
				public String apply(String t) {
					return "a";
				}
				
			}));
		} catch (Exception e) {
			return true; // will be handled by interpolative validator
		}
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
