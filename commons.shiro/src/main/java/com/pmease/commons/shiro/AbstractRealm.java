package com.pmease.commons.shiro;

import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.google.inject.Inject;
import com.pmease.commons.loader.AppLoader;

public abstract class AbstractRealm extends AuthorizingRealm {

	@Inject
	public AbstractRealm() {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
	}
	
	@SuppressWarnings("serial")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		final Long userId = (Long) principals.getPrimaryPrincipal();
		
		return new AuthorizationInfo() {
			
			@Override
			public Collection<String> getStringPermissions() {
				return new HashSet<>();
			}
			
			@Override
			public Collection<String> getRoles() {
				return new HashSet<>();
			}
			
			@Override
			public Collection<Permission> getObjectPermissions() {
				return permissionsOf(userId);
			}
		};
	}
	
	/**
	 * Retrieve {@link AuthenticationInfo} of specified token. 
	 * 
	 * @param token
	 * 			The token used to retrieve associated {@link AuthenticationInfo}
	 * @return
	 * 			{@link AuthenticationInfo} of specified token. Specifically if {@link AuthenticationInfo#getCredentials()}
	 * 			returns <tt>null</tt>, the credential will not be used to match against the password saved in token and 
	 * 			the authentication is considered successful. This is typically the case when the user is already 
	 * 			authenticated against other systems (such as LDAP) in this method. 
	 * @throws 
	 * 			AuthenticationException
	 */
	protected AuthenticationInfo authenticationInfoOf(UsernamePasswordToken token) throws AuthenticationException {
		return getUserByName(token.getUsername());
	}

	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		return authenticationInfoOf((UsernamePasswordToken) token);
	}
	
	protected abstract Collection<Permission> permissionsOf(Long userId);
	
	/**
	 * Get user by name.
	 * 
	 * @param userName
	 * 			name of the user
	 * @return
	 * 			user with specified name, or <tt>null</tt> if not found
	 */
	protected abstract @Nullable AbstractUser getUserByName(String userName);

	@Override
	protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
		/*
		 * A null value of credentials in AuthenticationInfo means that we should not check credentials. Typically 
		 * this is set to null when the token has already been authenticated at the time generating the 
		 * AuthenticationInfo, for instance, when submitting the token to third party system for authentication. 
		 */
		if (info.getCredentials() != null)
			super.assertCredentialsMatch(token, info);
	}

}
