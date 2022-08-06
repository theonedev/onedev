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
import io.onedev.server.entitymanager.IssueAuthorizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.permission.ConfidentialIssuePermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.UserAdministration;

public abstract class AbstractAuthorizingRealm extends AuthorizingRealm {

    protected final UserManager userManager;
    
    protected final EmailAddressManager emailAddressManager;
    
    protected final GroupManager groupManager;
    
    protected final ProjectManager projectManager;
    
    protected final SessionManager sessionManager;
    
    private final IssueAuthorizationManager issueAuthorizationManager;
    
    protected final SettingManager settingManager;
    
    @SuppressWarnings("serial")
	private static final MetaDataKey<Map<Long, AuthorizationInfo>> AUTHORIZATION_INFOS = 
			new MetaDataKey<Map<Long, AuthorizationInfo>>() {};    
    
	@Inject
    public AbstractAuthorizingRealm(UserManager userManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, 
    		SettingManager settingManager, EmailAddressManager emailAddressManager, 
    		IssueAuthorizationManager issueAuthorizationManager) {
    	this.userManager = userManager;
    	this.groupManager = groupManager;
    	this.projectManager = projectManager;
    	this.sessionManager = sessionManager;
    	this.settingManager = settingManager;
    	this.emailAddressManager = emailAddressManager;
    	this.issueAuthorizationManager = issueAuthorizationManager;
    }

	private AuthorizationInfo newAuthorizationInfo(Long userId) {
		Collection<Permission> permissions = sessionManager.call(new Callable<Collection<Permission>>() {

			@Override
			public Collection<Permission> call() throws Exception {
				Collection<Permission> permissions = new ArrayList<>();
				permissions.add(new Permission() {

					@Override
					public boolean implies(Permission p) {
				        if (userId != 0L) { 
				            User user = userManager.load(userId);
				        	if (user.isRoot() || user.isSystem()) 
				        		return true;
				        	if (new UserAdministration(user).implies(p))
				        		return true;
				           	for (Group group: user.getGroups()) {
				           		if (group.implies(p))
				           			return true;
				           	} 
				           	Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
			           		if (defaultLoginGroup != null && defaultLoginGroup.implies(p)) 
			           			return true;
				           	
			           		if (p instanceof ProjectPermission) {
					        	ProjectPermission projectPermission = (ProjectPermission) p;
					        	if (projectPermission.getPrivilege() instanceof ConfidentialIssuePermission) {
					        		ConfidentialIssuePermission confidentialIssuePermission = 
					        				(ConfidentialIssuePermission) projectPermission.getPrivilege();
					        		if (issueAuthorizationManager.find(confidentialIssuePermission.getIssue(), user) != null)
					        			return true;
					        	}
					        	for (UserAuthorization authorization: user.getProjectAuthorizations()) { 
			    					if (new ProjectPermission(authorization.getProject(), authorization.getRole()).implies(p))
			    						return true;
					        	}
			           		}
				        } 
				        if (p instanceof ProjectPermission 
				        		&& (userId != 0L || settingManager.getSecuritySetting().isEnableAnonymousAccess())) {
				        	ProjectPermission projectPermission = (ProjectPermission) p;
				        	Project project = projectPermission.getProject();
				        	Permission privilege = projectPermission.getPrivilege();
				        	do {
				        		if (project.getDefaultRole() != null && project.getDefaultRole().implies(privilege))
				        			return true;
				        		project = project.getParent();
				        	} while (project != null);
				        }
						return false;
					}
					
				});
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