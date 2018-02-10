package com.turbodev.server.web.page.user;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;

import com.turbodev.server.security.SecurityUtils;
import com.turbodev.server.web.util.WicketUtils;

public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, String> {

	public void initialize(CurrentPassword constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value != null) {
			UserPage page = (UserPage) WicketUtils.getPage();
			
			AuthenticationToken token = new UsernamePasswordToken(page.getUser().getName(), value);
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
