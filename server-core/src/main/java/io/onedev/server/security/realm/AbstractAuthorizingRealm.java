package io.onedev.server.security.realm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.entitymanager.EmailAddressManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.permission.ConfidentialIssuePermission;
import io.onedev.server.security.permission.CreateRootProjects;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UserAdministration;

public abstract class AbstractAuthorizingRealm extends AuthorizingRealm {

    protected final UserManager userManager;
    
    protected final EmailAddressManager emailAddressManager;
    
    protected final GroupManager groupManager;
    
    protected final ProjectManager projectManager;
    
    protected final SessionManager sessionManager;
    
    protected final SettingManager settingManager;
    
    @SuppressWarnings("serial")
	private static final MetaDataKey<Map<Long, AuthorizationInfo>> AUTHORIZATION_INFOS = 
			new MetaDataKey<Map<Long, AuthorizationInfo>>() {};    
    
	@Inject
    public AbstractAuthorizingRealm(UserManager userManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, 
    		SettingManager settingManager, EmailAddressManager emailAddressManager) {
    	this.userManager = userManager;
    	this.groupManager = groupManager;
    	this.projectManager = projectManager;
    	this.sessionManager = sessionManager;
    	this.settingManager = settingManager;
    	this.emailAddressManager = emailAddressManager;
    }

	private Collection<Permission> getGroupPermissions(Group group, User user) {
		Collection<Permission> permissions = new ArrayList<>();
		if (group.isAdministrator()) 
			permissions.add(new SystemAdministration());
		else if (group.isCreateRootProjects()) 
			permissions.add(new CreateRootProjects());
		for (GroupAuthorization authorization: group.getAuthorizations()) 
			permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
		return permissions;
	}
	
	private AuthorizationInfo newAuthorizationInfo(Long userId) {
		Collection<Permission> permissions = sessionManager.call(new Callable<Collection<Permission>>() {

			@Override
			public Collection<Permission> call() throws Exception {
				Collection<Permission> permissions = new ArrayList<>();

		        if (userId != 0L) { 
		            User user = userManager.load(userId);
		        	if (user.isRoot() || user.isSystem()) 
		        		permissions.add(new SystemAdministration());
		        	permissions.add(new UserAdministration(user));
		           	for (Group group: user.getGroups())
		           		permissions.addAll(getGroupPermissions(group, user));
		           	Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
	           		if (defaultLoginGroup != null) 
	           			permissions.addAll(getGroupPermissions(defaultLoginGroup, user));
		           	
		        	for (UserAuthorization authorization: user.getProjectAuthorizations()) 
    					permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
		        	for (IssueAuthorization authorization: user.getIssueAuthorizations()) {
    					permissions.add(new ProjectPermission(
    							authorization.getIssue().getProject(), 
    							new ConfidentialIssuePermission(authorization.getIssue())));
		        	}
		        } 
		        if (userId != 0L || settingManager.getSecuritySetting().isEnableAnonymousAccess()) {
			        for (Project project: projectManager.query()) {
			        	if (project.getDefaultRole() != null)
			        		permissions.add(new ProjectPermission(project, project.getDefaultRole()));
			        }
		        }
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

}