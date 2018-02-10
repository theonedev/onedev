package com.turbodev.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.turbodev.launcher.loader.AppLoader;
import com.turbodev.utils.StringUtils;
import com.google.common.base.Throwables;
import com.turbodev.server.manager.CacheManager;
import com.turbodev.server.manager.ConfigManager;
import com.turbodev.server.manager.GroupManager;
import com.turbodev.server.manager.MembershipManager;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.Group;
import com.turbodev.server.model.Membership;
import com.turbodev.server.model.User;
import com.turbodev.server.persistence.annotation.Sessional;
import com.turbodev.server.persistence.annotation.Transactional;
import com.turbodev.server.security.authenticator.Authenticated;
import com.turbodev.server.security.authenticator.Authenticator;
import com.turbodev.server.security.permission.CreateProjects;
import com.turbodev.server.security.permission.ProjectPermission;
import com.turbodev.server.security.permission.PublicPermission;
import com.turbodev.server.security.permission.SystemAdministration;
import com.turbodev.server.security.permission.UserAdministration;
import com.turbodev.server.util.facade.GroupAuthorizationFacade;
import com.turbodev.server.util.facade.GroupFacade;
import com.turbodev.server.util.facade.MembershipFacade;
import com.turbodev.server.util.facade.UserAuthorizationFacade;
import com.turbodev.server.util.facade.UserFacade;

@Singleton
public class TurboDevAuthorizingRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(TurboDevAuthorizingRealm.class);
	
    private final UserManager userManager;
    
    private final CacheManager cacheManager;
    
    private final ConfigManager configManager;
    
    private final MembershipManager membershipManager;
    
    private final GroupManager groupManager;
    
	@Inject
    public TurboDevAuthorizingRealm(UserManager userManager, CacheManager cacheManager, ConfigManager configManager, 
    		MembershipManager membershipManager, GroupManager groupManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.userManager = userManager;
    	this.cacheManager = cacheManager;
    	this.configManager = configManager;
    	this.membershipManager = membershipManager;
    	this.groupManager = groupManager;
    }

	@Sessional
	protected Collection<Permission> getObjectPermissionsInSession(Long userId) {
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
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		return new AuthorizationInfo() {
			
			private static final long serialVersionUID = 1L;

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
				return getObjectPermissionsInSession((Long) principals.getPrimaryPrincipal());
			}
		};
	}
	
	@Transactional
	protected AuthenticationInfo doGetAuthenticationInfoInTransaction(AuthenticationToken token) 
			throws AuthenticationException {
    	User user = userManager.findByName(((UsernamePasswordToken) token).getUsername());
    	if (user != null && user.isRoot())
    		return user;

    	if (user == null || StringUtils.isBlank(user.getPassword())) {
        	Authenticator authenticator = configManager.getAuthenticator();
        	if (authenticator != null) {
        		Authenticated authenticated;
        		try {
        			authenticated = authenticator.authenticate((UsernamePasswordToken) token);
        		} catch (Throwable e) {
        			if (e instanceof AuthenticationException) {
        				logger.debug("Authentication not passed", e);
            			throw Throwables.propagate(e);
        			} else {
        				logger.error("Error authenticating user", e);
            			throw new AuthenticationException("Error authenticating user", e);
        			}
        		}
    			if (user != null) {
    				if (authenticated.getEmail() != null)
    					user.setEmail(authenticated.getEmail());
    				if (authenticated.getFullName() != null)
    					user.setFullName(authenticated.getFullName());

    				Collection<String> existingGroupNames = new HashSet<>();
    				for (Membership membership: user.getMemberships()) 
    					existingGroupNames.add(membership.getGroup().getName());
    				if (!authenticated.getGroupNames().isEmpty()) {
    					Collection<String> retrievedGroupNames = new HashSet<>();
    					for (String groupName: authenticated.getGroupNames()) {
    						Group group = groupManager.find(groupName);
    						if (group != null) {
    							if (!existingGroupNames.contains(groupName)) {
    								Membership membership = new Membership();
    								membership.setGroup(group);
    								membership.setUser(user);
    								membershipManager.save(membership);
    								user.getMemberships().add(membership);
    								existingGroupNames.add(groupName);
    							}
    							retrievedGroupNames.add(groupName);
    						} else {
    							logger.debug("Group '{}' from external authenticator is not defined", groupName);
    						}
    					}
        				for (Iterator<Membership> it = user.getMemberships().iterator(); it.hasNext();) {
        					Membership membership = it.next();
        					if (!retrievedGroupNames.contains(membership.getGroup().getName())) {
        						it.remove();
        						membershipManager.delete(membership);
        					}
        				}
    				}
    				userManager.save(user);
    			} else {
    				user = new User();
    				user.setName(((UsernamePasswordToken) token).getUsername());
    				user.setPassword("");
    				if (authenticated.getEmail() != null)
    					user.setEmail(authenticated.getEmail());
    				if (authenticated.getFullName() != null)
    					user.setFullName(authenticated.getFullName());
    				userManager.save(user);
    				if (authenticated.getGroupNames().isEmpty()) {
    					for (String groupName: authenticator.getDefaultGroupNames()) {
    						Group group = groupManager.find(groupName);
    						if (group != null) {
    							Membership membership = new Membership();
    							membership.setGroup(group);
    							membership.setUser(user);
    							user.getMemberships().add(membership);
    							membershipManager.save(membership);
    						} else {
    							logger.warn("Default group '{}' of external authenticator is not defined", groupName);
    						}
    					}
    				}
    			}
        	} else {
        		user = null;
        	}
    	}
    	
    	return user;		
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		// transaction annotation can not be applied to final method, so we relay to another method
		return doGetAuthenticationInfoInTransaction(token);
	}
	
}
