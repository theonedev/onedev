package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Patterns;

public class PatternsValidator implements ConstraintValidator<Patterns, String> {
	
	private String message;
	
	@Override
	public void initialize(Patterns constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
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
