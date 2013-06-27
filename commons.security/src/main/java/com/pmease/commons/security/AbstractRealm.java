package com.pmease.commons.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.inject.Provider;

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
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.util.ClassUtils;

public abstract class AbstractRealm<T extends AbstractUser> extends AuthorizingRealm {

	private Provider<GeneralDao> generalDaoProvider;
	
	protected final Class<T> userClass;

	@SuppressWarnings("unchecked")
	@Inject
	public AbstractRealm(Provider<GeneralDao> generalDaoProvider, CredentialsMatcher credentialsMatcher) {
		this.generalDaoProvider = generalDaoProvider;
		List<Class<?>> typeArguments = ClassUtils.getTypeArguments(AbstractRealm.class, getClass());
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
				return doGetPermissions(userId);
			}
			
			@Override
			public Collection<String> getRoles() {
				return new HashSet<String>();
			}
			
			@Override
			public Collection<Permission> getObjectPermissions() {
				return new HashSet<Permission>();
			}
		};
	}
	
	/**
	 * Get assigned permissions of user of specified identifier.
	 * @param userId
	 * 			Identifier of user to get permissions of. Value of <tt>0</tt> means anonymous user 
	 * @return
	 * 			Collection of {@link WildcardPermission} string
	 */
	protected abstract Collection<String> doGetPermissions(Long userId);

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        
        DetachedCriteria criteria = DetachedCriteria.forClass(userClass);
        criteria.add(Restrictions.eq("loginName", upToken.getUsername()));

        return (AuthenticationInfo) generalDaoProvider.get().find(criteria);
	}

}
