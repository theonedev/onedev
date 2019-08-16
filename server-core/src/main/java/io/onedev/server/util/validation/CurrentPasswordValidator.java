package io.onedev.server.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.validation.annotation.CurrentPassword;

public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, String> {

	@Override
	public void initialize(CurrentPassword constaintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value != null) {
			AuthenticationToken token = new UsernamePasswordToken(SecurityUtils.getUser().getName(), value);
			try {
				if (SecurityUtils.getSecurityManager().authenticate(token) != null)
					return true;
			} catch (Exception e) {
			}
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate("Current password does not match").addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
}
