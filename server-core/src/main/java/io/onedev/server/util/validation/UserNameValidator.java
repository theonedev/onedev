package io.onedev.server.util.validation;

import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.util.interpolative.Interpolative;
import io.onedev.server.util.validation.annotation.UserName;

public class UserNameValidator implements ConstraintValidator<UserName, String> {
	
	private boolean interpolative;
	
	private String message;
	
	@Override
	public void initialize(UserName constaintAnnotation) {
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
		
		if (value.equals("new")) {
			constraintContext.disableDefaultConstraintViolation();
			String message = this.message;
			if (message.length() == 0)
				message = "'" + value + "' is a reserved name";
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
