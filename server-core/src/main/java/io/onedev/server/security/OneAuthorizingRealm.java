package io.onedev.server.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.MembershipManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.administration.authenticator.Authenticated;
import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.security.permission.AccessBuildLog;
import io.onedev.server.security.permission.CreateProjects;
import io.onedev.server.security.permission.EditIssueField;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.ReadCode;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UserAdministration;

@Singleton
public class OneAuthorizingRealm extends AuthorizingRealm {

	private static final Logger logger = LoggerFactory.getLogger(OneAuthorizingRealm.class);
	
    private final UserManager userManager;
    
    private final SettingManager configManager;
    
    private final MembershipManager membershipManager;
    
    private final GroupManager groupManager;
    
    private final ProjectManager projectManager;
    
    private final TransactionManager transactionManager;
    
	@Inject
    public OneAuthorizingRealm(UserManager userManager, SettingManager configManager, 
    		MembershipManager membershipManager, GroupManager groupManager, 
    		ProjectManager projectManager, TransactionManager transactionManager) {
	    PasswordMatcher passwordMatcher = new PasswordMatcher();
	    passwordMatcher.setPasswordService(AppLoader.getInstance(PasswordService.class));
		setCredentialsMatcher(passwordMatcher);
		
    	this.userManager = userManager;
    	this.configManager = configManager;
    	this.membershipManager = membershipManager;
    	this.groupManager = groupManager;
    	this.projectManager = projectManager;
    	this.transactionManager = transactionManager;
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
			
			private Collection<Permission> getGroupPermissions(Group group, @Nullable User user) {
				Collection<Permission> permissions = new ArrayList<>();
        		if (group.isAdministrator()) {
        			if (user != null) {
        				permissions.add(new SystemAdministration());
        			} else {
        				for (Project project: projectManager.query()) {
        					permissions.add(new ProjectPermission(project, new ReadCode()));
        					for (FieldSpec field: OneDev.getInstance(SettingManager.class).getIssueSetting().getFieldSpecs())
        						permissions.add(new ProjectPermission(project, new EditIssueField(field.getName())));
        					permissions.add(new ProjectPermission(project, new JobPermission("*", new AccessBuildLog())));
        				}
        			}
        		}
        		if (user != null && group.isCreateProjects())
        			permissions.add(new CreateProjects());
        		for (GroupAuthorization authorization: group.getProjectAuthorizations()) 
					permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
				return permissions;
			}
			
			@Override
			public Collection<Permission> getObjectPermissions() {
				return transactionManager.getSessionManager().call(new Callable<Collection<Permission>>() {

					@Override
					public Collection<Permission> call() throws Exception {
						Long userId = (Long) principals.getPrimaryPrincipal();						
						Collection<Permission> permissions = new ArrayList<>();

						User user = null;
				        if (userId != 0L) { 
				            user = userManager.load(userId);
				        	if (user.isRoot()) 
				        		permissions.add(new SystemAdministration());
				        	permissions.add(new UserAdministration(user));
				           	for (Group group: user.getGroups())
				           		permissions.addAll(getGroupPermissions(group, user));
				        	for (UserAuthorization authorization: user.getProjectAuthorizations()) 
            					permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
				        	for (Project project: user.getProjects()) 
				        		permissions.add(new ProjectPermission(project, new ManageProject()));
				        } 
			        	Group group = groupManager.findAnonymous();
			        	if (group != null)
			           		permissions.addAll(getGroupPermissions(group, user));
						return permissions;
					}
					
				});
			}
		};
	}
	
	@Override
	protected final AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		return transactionManager.call(new Callable<AuthenticationInfo>() {

			@Override
			public AuthenticationInfo call() throws Exception {
		    	User user = userManager.findByName(((UsernamePasswordToken) token).getUsername());
		    	if (user != null && user.isRoot())
		    		return user;

		    	if (user == null || StringUtils.isBlank(user.getPassword())) {
		        	Authenticator authenticator = configManager.getAuthenticator();
		        	if (authenticator != null) {
		        		Authenticated authenticated;
		        		try {
		        			authenticated = authenticator.authenticate((UsernamePasswordToken) token);
		        		} catch (Exception e) {
		        			if (e instanceof AuthenticationException) {
		        				logger.debug("Authentication not passed", e);
		            			throw ExceptionUtils.unchecked(e);
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
		    				if (authenticated.getGroupNames().isEmpty() && authenticator.getDefaultGroup() != null) {
	    						Group group = groupManager.find(authenticator.getDefaultGroup());
	    						if (group != null) {
	    							Membership membership = new Membership();
	    							membership.setGroup(group);
	    							membership.setUser(user);
	    							user.getMemberships().add(membership);
	    							membershipManager.save(membership);
	    						} else {
	    							logger.error("Default group '{}' of external authenticator is not defined", 
	    									authenticator.getDefaultGroup());
	    						}
		    				}
		    			}
		        	} else {
		        		user = null;
		        	}
		    	}
		    	
		    	return user;		
			}
			
		});
	}
	
}
