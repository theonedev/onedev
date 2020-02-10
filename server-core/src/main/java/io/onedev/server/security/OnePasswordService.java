package io.onedev.server.security;

import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;

import com.pmease.security.shiro.bcrypt.BCryptPasswordService;

import io.onedev.server.model.User;

@Singleton
public class OnePasswordService implements PasswordService {

	private final PasswordService bcryptPasswordService = new BCryptPasswordService();
	
	@Override
	public String encryptPassword(Object plaintextPassword) throws IllegalArgumentException {
		return bcryptPasswordService.encryptPassword(plaintextPassword);
	}

	@Override
	public boolean passwordsMatch(Object submittedPlaintext, String encrypted) {
		if (!User.EXTERNAL_MANAGED.equals(encrypted))
			return bcryptPasswordService.passwordsMatch(submittedPlaintext, encrypted);
		else
			return true;
	}

}
