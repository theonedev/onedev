package io.onedev.server.util.validation;

import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Patterns;

public class PatternsValidator implements ConstraintValidator<Patterns, String> {
	
	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(Patterns constaintAnnotation) {
		interpolative = constaintAnnotation.interpolative();
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		if (interpolative && !Interpolated.get()) try {
			value = Interpolative.parse(value).interpolateWith(new Function<String, String>() {

				@Override
				public String apply(String t) {
					return "a";
				}
				
			});
		} catch (Exception e) {
			return true; // will be handled by interpolative validator
		}
		try {
			PatternSet.parse(value);
			return true;
		} catch (Exception e) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				if (StringUtils.isNotBlank(e.getMessage()))
					message = e.getMessage();
				else
					message = "Malformed pattern set";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
}
