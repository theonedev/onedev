package com.pmease.commons.shiro;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import com.google.inject.Inject;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.ReflectionUtils;

public abstract class AbstractRealm<T extends AbstractUser> extends AuthorizingRealm {

	private GeneralDao generalDao;
	
	private final Class<T> userClass;

	@SuppressWarnings("unchecked")
	@Inject
	public AbstractRealm(GeneralDao generalDao, CredentialsMatcher credentialsMatcher) {
		this.generalDao = generalDao;
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(AbstractRealm.class, getClass());
		userClass = ((Class<T>) typeArguments.get(0));

		setCredentialsMatcher(credentialsMatcher);
	}
	
	@SuppressWarnings("serial")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		final Long userId = (Long) principals.getPrimaryPrincipal();
		
		return new AuthorizationInfo() {
			
			@Override
			public Collection<String> getStringPermissions() {
				return new HashSet<String>();
			}
			
			@Override
			public Collection<String> getRoles() {
				return new HashSet<String>();
			}
			
			@Override
			public Collection<Permission> getObjectPermissions() {
				return permissionsOf(userId);
			}
		};
	}
	
	/**
	 * Get assigned permissions of user of specified identifier.
	 * 
	 * @param userId
	 * 			Identifier of user to get permissions of. Value of <tt>0</tt> means anonymous user 
	 * @return
	 * 			Collection of {@link WildcardPermission} string
	 */
	protected abstract Collection<Permission> permissionsOf(Long userId);

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
		DetachedCriteria criteria = DetachedCriteria.forClass(userClass);
        criteria.add(Restrictions.eq("name", token.getUsername()));

        return (AbstractUser) generalDao.find(criteria);
	}

	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		return authenticationInfoOf((UsernamePasswordToken) token);
	}

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
