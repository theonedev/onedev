package io.onedev.server.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Password;
import io.onedev.server.service.SettingService;

public class PasswordValidator implements ConstraintValidator<Password, String> {
	
	private String message;

	private boolean checkPolicy;
	
	@Override
	public void initialize(Password constaintAnnotation) {
		message = constaintAnnotation.message();
		checkPolicy = constaintAnnotation.checkPolicy();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value == null || !checkPolicy)
			return true;
		
		var passwordPolicy = OneDev.getInstance(SettingService.class).getSecuritySetting().getPasswordPolicy();
		if (passwordPolicy != null) {
			var error = passwordPolicy.checkPassword(value);
			if (error != null) {
				String message = this.message;
				if (message.length() == 0) {
					message = error;
				}
				constraintContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
				return false;
			}
		}
		return true;
	}
	
}
