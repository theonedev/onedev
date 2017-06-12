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
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.security.permission.CreateProjects;
import com.gitplex.server.security.permission.ProjectPermission;
import com.gitplex.server.security.permission.PublicPermission;
import com.gitplex.server.security.permission.SystemAdministration;
import com.gitplex.server.security.permission.UserAdministration;
import com.gitplex.server.util.facade.GroupAuthorizationFacade;
import com.gitplex.server.util.facade.GroupFacade;
import com.gitplex.server.util.facade.MembershipFacade;
import com.gitplex.server.util.facade.UserAuthorizationFacade;
import com.gitplex.server.util.facade.UserFacade;

@Singleton
public class SecurityRealm extends AuthorizingRealm {

    private final UserManager userManager;
    
    private final CacheManager cacheManager;
    
    private final ConfigManager configManager;
    
	@Inject
    public SecurityRealm(UserManager userManager, CacheManager cacheManager, ConfigManager configManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.userManager = userManager;
    	this.cacheManager = cacheManager;
    	this.configManager = configManager;
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

				UserFacade user = null;
                if (userId != 0L) 
                    user = cacheManager.getUser(userId);
                if (user != null) {
					permissions.add(new PublicPermission());
                	if (user.isRoot()) 
                		permissions.add(new SystemAdministration());
                	permissions.add(new UserAdministration(user));
                	for (MembershipFacade membership: cacheManager.getMemberships().values()) {
                		if (membership.getUserId().equals(userId)) {
                			GroupFacade group = cacheManager.getGroup(membership.getGroupId());
                    		if (group.isAdministrator())
                    			permissions.add(new SystemAdministration());
                    		if (group.isCanCreateProjects())
                    			permissions.add(new CreateProjects());
                    		for (GroupAuthorizationFacade authorization: 
                    				cacheManager.getGroupAuthorizations().values()) {
                    			if (authorization.getGroupId().equals(group.getId())) {
                        			permissions.add(new ProjectPermission(
                        					cacheManager.getProject(authorization.getProjectId()), 
                        					authorization.getPrivilege()));
                    			}
                    		}
                		}
                	}
                	for (UserAuthorizationFacade authorization: cacheManager.getUserAuthorizations().values()) {
                		if (authorization.getUserId().equals(userId)) {
                    		permissions.add(new ProjectPermission(
                    				cacheManager.getProject(authorization.getProjectId()), 
                    				authorization.getPrivilege()));
                		}
                	}
                } else if (configManager.getSecuritySetting().isEnableAnonymousAccess()) {
					permissions.add(new PublicPermission());
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
