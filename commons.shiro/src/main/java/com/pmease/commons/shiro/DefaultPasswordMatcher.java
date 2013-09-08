package com.pmease.commons.shiro;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;

@Singleton
public class DefaultPasswordMatcher extends PasswordMatcher {

	@Inject
	public DefaultPasswordMatcher(PasswordService passwordService) {
		setPasswordService(passwordService);
	}
	
}
