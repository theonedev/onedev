package io.onedev.server.util.validation;

import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.web.editable.annotation.Numeric;

public class NumericValidator implements ConstraintValidator<Numeric, String> {

	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(Numeric constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null)
			return true;
		
		if (interpolative && !Interpolated.get()) try {
			value = StringUtils.unescape(Interpolative.parse(value).interpolateWith(new Function<String, String>() {

				@Override
				public String apply(String t) {
					return "1";
				}
				
			}));
		} catch (Exception e) {
			return true; // will be handled by interpolative validator
		}
		
		try {
			Long.parseLong(value);
		} catch (NumberFormatException e) {
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
