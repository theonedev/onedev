package io.onedev.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;
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
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.SshKeyManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.administration.sso.SsoAuthenticated;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.CreateProjects;
import io.onedev.server.security.permission.EditIssueField;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UserAdministration;

@Singleton
public class OneAuthorizingRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(OneAuthorizingRealm.class);
	
    private final UserManager userManager;
    
    private final SettingManager settingManager;
    
    private final MembershipManager membershipManager;
    
    private final GroupManager groupManager;
    
    private final ProjectManager projectManager;
    
    private final SessionManager sessionManager;
    
    private final TransactionManager transactionManager;
    
    private final SshKeyManager sshKeyManager;
    
    @SuppressWarnings("serial")
	private static final MetaDataKey<Map<Long, AuthorizationInfo>> AUTHORIZATION_INFOS = 
			new MetaDataKey<Map<Long, AuthorizationInfo>>() {};    
    
	@Inject
    public OneAuthorizingRealm(UserManager userManager, SettingManager settingManager, 
    		MembershipManager membershipManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, 
    		TransactionManager transactionManager, SshKeyManager sshKeyManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.userManager = userManager;
    	this.settingManager = settingManager;
    	this.membershipManager = membershipManager;
    	this.groupManager = groupManager;
    	this.projectManager = projectManager;
    	this.sessionManager = sessionManager;
    	this.transactionManager = transactionManager;
    	this.sshKeyManager = sshKeyManager;
    }

	private Collection<Permission> getGroupPermissions(Group group, @Nullable User user) {
		Collection<Permission> permissions = new ArrayList<>();
		if (group.isAdministrator()) {
			if (user != null) {
				permissions.add(new SystemAdministration());
			} else {
				for (Project project: projectManager.query()) {
					permissions.add(new ProjectPermission(project, new ReadCode()));
					for (FieldSpec field: OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpecs())
						permissions.add(new ProjectPermission(project, new EditIssueField(Sets.newHashSet(field.getName()))));
					permissions.add(new ProjectPermission(project, new JobPermission("*", new AccessBuildLog())));
				}
			}
		}
		if (user != null && group.isCreateProjects())
			permissions.add(new CreateProjects());
		for (GroupAuthorization authorization: group.getAuthorizations()) 
			permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
		return permissions;
	}
	
	private AuthorizationInfo newAuthorizationInfo(Long userId) {
		Collection<Permission> permissions = sessionManager.call(new Callable<Collection<Permission>>() {

			@Override
			public Collection<Permission> call() throws Exception {
				Collection<Permission> permissions = new ArrayList<>();

				User user = null;
		        if (userId != 0L) { 
		            user = userManager.load(userId);
		        	if (user.isRoot() || user.isSystem()) 
		        		permissions.add(new SystemAdministration());
		        	permissions.add(new UserAdministration(user));
		           	for (Group group: user.getGroups())
		           		permissions.addAll(getGroupPermissions(group, user));
		        	for (UserAuthorization authorization: user.getAuthorizations()) 
    					permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
		        } 
	        	Group group = groupManager.findAnonymous();
	        	if (group != null)
	           		permissions.addAll(getGroupPermissions(group, user));
				return permissions;
			}
			
		});
		
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
				return permissions;
			}
		};		
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Long userId = (Long) principals.getPrimaryPrincipal();						
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			Map<Long, AuthorizationInfo> authorizationInfos = requestCycle.getMetaData(AUTHORIZATION_INFOS);
			if (authorizationInfos == null) {
				authorizationInfos = new HashMap<>();
				requestCycle.setMetaData(AUTHORIZATION_INFOS, authorizationInfos);
			}
			AuthorizationInfo authorizationInfo = authorizationInfos.get(userId);
			if (authorizationInfo == null) {
				authorizationInfo = newAuthorizationInfo(userId);
				authorizationInfos.put(userId, authorizationInfo);
			}
			return authorizationInfo;
		} else {
			return newAuthorizationInfo(userId);
		}
	}
	
	private User newUser(String userName, Authenticated authenticated, 
			@Nullable SsoInfo ssoInfo, @Nullable String defaultGroup) {
		User user = new User();
		user.setName(userName);
		user.setPassword(User.EXTERNAL_MANAGED);
		user.setEmail(authenticated.getEmail());
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		user.setSsoInfo(ssoInfo);
		
		userManager.save(user);

		Collection<String> groupNames = authenticated.getGroupNames();
		if (groupNames == null && defaultGroup != null)
			groupNames = Sets.newHashSet(defaultGroup);
		if (groupNames != null) 
			membershipManager.syncMemberships(user, groupNames);
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
    	return user;
	}
	
	private void updateUser(User user, Authenticated authenticated, @Nullable String userName) {
		if (userName != null)
			user.setName(userName);
		user.setEmail(authenticated.getEmail());
		if (authenticated.getFullName() != null)
			user.setFullName(authenticated.getFullName());
		userManager.save(user);
		if (authenticated.getGroupNames() != null)
			membershipManager.syncMemberships(user, authenticated.getGroupNames());
		
    	if (authenticated.getSshKeys() != null)
    		sshKeyManager.syncSshKeys(user, authenticated.getSshKeys());
	}
	
	@Override
	public boolean supports(AuthenticationToken token) {
		return token instanceof UsernamePasswordToken || token instanceof SsoAuthenticated;
	}

	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) 
			throws AuthenticationException {
		return transactionManager.call(new Callable<AuthenticationInfo>() {

			@Override
			public AuthenticationInfo call() {
				try {
					String userName = (String) token.getPrincipal();
					User user;
					if (token instanceof UsernamePasswordToken) {
				    	user = userManager.findByName(userName);
				    	if (user == null) {
					    	Authenticator authenticator = settingManager.getAuthenticator();
			    			if (authenticator != null) {
			    				Authenticated authenticated = authenticator.authenticate((UsernamePasswordToken) token);
			    				if (userManager.findByEmail(authenticated.getEmail()) != null)
			    					throw new AuthenticationException("Email '" + authenticated.getEmail() + "' has already been used by another account");
			        			user = newUser(userName, authenticated, null, authenticator.getDefaultGroup());
			    			} 
				    	} else if (user.getPassword().equals(User.EXTERNAL_MANAGED)) {
			    			if (user.getSsoInfo() != null) {
			    				throw new AuthenticationException("Account '" + userName 
			    						+ "' is set to authenticate via " + User.AUTH_SOURCE_SSO_PROVIDER + user.getSsoInfo().getConnector());
			    			}
					    	Authenticator authenticator = settingManager.getAuthenticator();
			    			if (authenticator != null) {
			    				Authenticated authenticated = authenticator.authenticate((UsernamePasswordToken) token);
			    				if (!authenticated.getEmail().equals(user.getEmail()) && userManager.findByEmail(authenticated.getEmail()) != null) 
			    					throw new AuthenticationException("Email '" + authenticated.getEmail() + "' has already been used by another account");
			        			updateUser(user, authenticated, null);
			    			} else {
			    				throw new AuthenticationException("Account '" + userName + "' is set to authenticate "
			    						+ "externally but " + User.AUTH_SOURCE_EXTERNAL_AUTHENTICATOR + " is not defined");
			    			}
				    	} 				
					} else {
						SsoAuthenticated authenticated = (SsoAuthenticated) token;
						SsoInfo ssoInfo = authenticated.getSsoInfo();
				    	user = userManager.findBySsoInfo(ssoInfo);
				    	if (user == null) {
				    		if (userManager.findByName(userName) != null)
				    			throw new AuthenticationException("Account '" + userName + "' already exists");
				    		if (userManager.findByEmail(authenticated.getEmail()) != null)
				    			throw new AuthenticationException("Email '" + authenticated.getEmail() + "' has already been used by another account");
			    			user = newUser(userName, authenticated, ssoInfo, authenticated.getConnector().getDefaultGroup());
				    	} else {
				    		if (!authenticated.getUserName().equals(user.getName()) && userManager.findByName(authenticated.getUserName()) != null)
				    			throw new AuthenticationException("Account '" + userName + "' already exists");
				    		if (!authenticated.getEmail().equals(user.getEmail()) && userManager.findByEmail(authenticated.getEmail()) != null)
				    			throw new AuthenticationException("Email '" + authenticated.getEmail() + "' has already been used by another account");
			    			updateUser(user, authenticated, authenticated.getUserName());
				    	}				
					}
			    	return user;
				} catch (Exception e) {
	    			if (e instanceof AuthenticationException) {
	    				logger.debug("Authentication not passed", e);
	        			throw ExceptionUtils.unchecked(e);
	    			} else {
	    				logger.error("Error authenticating user", e);
	        			throw new AuthenticationException("Error authenticating user", e);
	    			}
				}
			}
		});
	}
}
