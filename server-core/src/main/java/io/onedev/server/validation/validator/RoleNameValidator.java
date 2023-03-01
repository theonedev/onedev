package io.onedev.server.validation.validator;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.annotation.RoleName;

public class RoleNameValidator implements ConstraintValidator<RoleName, String> {
	
	private static final Pattern PATTERN = Pattern.compile("[^\\[\\]<>\\{\\}]+");
	
	private String message;
	
	@Override
	public void initialize(RoleName constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null) 
			return true;
		
		if (!PATTERN.matcher(value).matches()) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0) {
				message = "Role name can not container characters '[', ']', '<', '>', '{', and '}'";
			}
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
		
}
