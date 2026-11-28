package io.onedev.server.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.CurrentPassword;
import io.onedev.server.security.AuthenticatingService;
import io.onedev.server.security.SecurityUtils;

public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, String> {

	private String message;
	
	@Override
	public void initialize(CurrentPassword constaintAnnotation) {
		message = constaintAnnotation.message();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value != null) {
			AuthenticationToken token = new UsernamePasswordToken(SecurityUtils.getAuthUser().getName(), value);
			try {
				if (OneDev.getInstance(AuthenticatingService.class).getAuthenticationInfo(token) != null)
					return true;
			} catch (Exception e) {
			}
			constraintContext.disableDefaultConstraintViolation();
			constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
}
