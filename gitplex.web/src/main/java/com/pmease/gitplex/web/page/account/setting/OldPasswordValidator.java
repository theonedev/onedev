package com.pmease.gitplex.web.page.account.setting;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.shiro.authc.credential.PasswordService;

import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.wicket.WicketUtils;
import com.pmease.gitplex.web.page.account.AccountPage;

public class OldPasswordValidator implements ConstraintValidator<OldPassword, String> {

	public void initialize(OldPassword constaintAnnotation) {
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintContext) {
		if (value != null) {
			AccountPage page = (AccountPage) WicketUtils.getPage();
			PasswordService passwordService = AppLoader.getInstance(PasswordService.class);
			if (!passwordService.passwordsMatch(value, (String) page.getAccount().getCredentials())) {
				constraintContext.disableDefaultConstraintViolation();
				constraintContext.buildConstraintViolationWithTemplate("Old password does not match").addConstraintViolation();
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}
}
