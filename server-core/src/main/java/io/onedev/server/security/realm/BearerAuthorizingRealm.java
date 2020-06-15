package io.onedev.server.security.realm;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;

import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.BearerAuthenticationToken;

@Singleton
public class BearerAuthorizingRealm extends AbstractAuthorizingRealm {

	@Inject
    public BearerAuthorizingRealm(UserManager userManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager) {
		super(userManager, groupManager, projectManager, sessionManager);
		setCredentialsMatcher(new AllowAllCredentialsMatcher());
    }

	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof BearerAuthenticationToken;
	}

	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		return (AuthenticationInfo) ((BearerAuthenticationToken) token).getPrincipal();
	}
	
}
