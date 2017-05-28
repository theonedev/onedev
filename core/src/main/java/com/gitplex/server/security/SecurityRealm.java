package com.gitplex.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.inject.Inject;
import javax.inject.Singleton;

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

import com.gitplex.launcher.loader.AppLoader;
import com.gitplex.server.manager.MembershipManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.GroupAuthorization;
import com.gitplex.server.model.Membership;
import com.gitplex.server.model.User;
import com.gitplex.server.model.UserAuthorization;
import com.gitplex.server.security.permission.CreateProjects;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.security.permission.PublicPermission;
import com.gitplex.server.security.permission.SystemAdministration;
import com.gitplex.server.security.permission.UserAdministration;

@Singleton
public class SecurityRealm extends AuthorizingRealm {

    private final UserManager userManager;
    
	@Inject
    public SecurityRealm(UserManager userManager, MembershipManager groupMembershipManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.userManager = userManager;
    }

	@SuppressWarnings("serial")
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Long userId = (Long) principals.getPrimaryPrincipal();
		
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
				Collection<Permission> permissions = new ArrayList<>();
				permissions.add(new PublicPermission());
                if (userId != 0L) {
                    User user = userManager.get(userId);
                    if (user != null) {
                    	if (user.isRoot()) 
                    		permissions.add(new SystemAdministration());
                    	permissions.add(new UserAdministration(user));
                    	for (Membership membership: user.getMemberships()) {
                    		if (membership.getGroup().isAdministrator())
                    			permissions.add(new SystemAdministration());
                    		if (membership.getGroup().isCanCreateProjects())
                    			permissions.add(new CreateProjects());
                    		for (GroupAuthorization authorization: membership.getGroup().getAuthorizations()) {
                    			permissions.add(new ProjectPermission(
                    					authorization.getProject(), authorization.getPrivilege()));
                    		}
                    	}
                    	for (UserAuthorization authorization: user.getAuthorizations()) {
                    		permissions.add(new ProjectPermission(
                    				authorization.getProject(), authorization.getPrivilege()));
                    	}
                    }
                }
				return permissions;
			}
		};
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
    	return userManager.findByName(((UsernamePasswordToken) token).getUsername());
	}
	
}
