package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.web.editable.annotation.UserMatcher;

public class UserMatcherValidator implements ConstraintValidator<UserMatcher, String> {

	@Override
	public void initialize(UserMatcher constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) {
			return true;
		} else {
			try {
				io.onedev.server.util.usermatcher.UserMatcher.parse(value);
				return true;
			} catch (Exception e) {
				constraintContext.disableDefaultConstraintViolation();
				String message;
				if (StringUtils.isNotBlank(e.getMessage()))
					message = e.getMessage();
				else
					message = "Malformed user matcher";
				
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
	}
	
}
