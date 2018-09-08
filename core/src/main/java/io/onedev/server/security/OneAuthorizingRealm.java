package io.onedev.server.security;

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

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.TeamManager;
import io.onedev.server.manager.MembershipManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.model.support.authenticator.Authenticated;
import io.onedev.server.model.support.authenticator.Authenticator;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.security.permission.CreateProjects;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UserAdministration;
import io.onedev.server.util.facade.MembershipFacade;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.TeamFacade;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.utils.StringUtils;

import com.google.common.base.Throwables;

@Singleton
public class OneAuthorizingRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(OneAuthorizingRealm.class);
	
    private final UserManager userManager;
    
    private final CacheManager cacheManager;
    
    private final SettingManager configManager;
    
    private final MembershipManager membershipManager;
    
    private final TeamManager teamManager;
    
	@Inject
    public OneAuthorizingRealm(UserManager userManager, CacheManager cacheManager, SettingManager configManager, 
    		MembershipManager membershipManager, TeamManager teamManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.userManager = userManager;
    	this.cacheManager = cacheManager;
    	this.configManager = configManager;
    	this.membershipManager = membershipManager;
    	this.teamManager = teamManager;
    }

	private Collection<Permission> getDefaultPermissions() {
		Collection<Permission> defaultPermissions = new ArrayList<>();
		for (ProjectFacade project: cacheManager.getProjects().values()) {
			if (project.getDefaultPrivilege() != null)
				defaultPermissions.add(new ProjectPermission(project, project.getDefaultPrivilege()));
		}
		return defaultPermissions;
	}
	
	@Sessional
	protected Collection<Permission> getObjectPermissionsInSession(Long userId) {
		Collection<Permission> permissions = new ArrayList<>();

		UserFacade user = null;
        if (userId != 0L) 
            user = cacheManager.getUser(userId);
        if (user != null) {
			permissions.addAll(getDefaultPermissions());
        	if (user.isRoot() || user.isAdministrator()) 
        		permissions.add(new SystemAdministration());
        	if (user.isCanCreateProjects())
        		permissions.add(new CreateProjects());
        	permissions.add(new UserAdministration(user));
        	for (MembershipFacade membership: cacheManager.getMemberships().values()) {
        		if (membership.getUserId().equals(userId)) {
        			TeamFacade team = cacheManager.getTeam(membership.getTeamId());
        			permissions.add(new ProjectPermission(cacheManager.getProject(team.getProjectId()), team.getPrivilege()));
        		}
        	}
        } else if (configManager.getSecuritySetting().isEnableAnonymousAccess()) {
			permissions.addAll(getDefaultPermissions());
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

    				Collection<String> existingTeamFQNs = new HashSet<>();
    				for (Membership membership: user.getMemberships()) { 
    					Team team = membership.getTeam();
    					existingTeamFQNs.add(team.getFQN());
    				}
    				if (!authenticated.getTeamFQNs().isEmpty()) {
    					Collection<String> retrievedTeamFQNs = new HashSet<>();
    					for (String teamFQN: authenticated.getTeamFQNs()) {
    						Team team = teamManager.findByFQN(teamFQN);
    						if (team != null) {
    							if (!existingTeamFQNs.contains(teamFQN)) {
    								Membership membership = new Membership();
    								membership.setTeam(team);
    								membership.setUser(user);
    								membershipManager.save(membership);
    								user.getMemberships().add(membership);
    								existingTeamFQNs.add(teamFQN);
    							}
    							retrievedTeamFQNs.add(teamFQN);
    						} else {
    							logger.debug("Team '{}' from external authenticator is not defined", teamFQN);
    						}
    					}
        				for (Iterator<Membership> it = user.getMemberships().iterator(); it.hasNext();) {
        					Membership membership = it.next();
        					if (!retrievedTeamFQNs.contains(membership.getTeam().getFQN())) {
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
