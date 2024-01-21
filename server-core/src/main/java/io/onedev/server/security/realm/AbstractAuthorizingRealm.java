package io.onedev.server.security.realm;

import com.google.common.collect.Lists;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.*;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.security.permission.*;
import io.onedev.server.util.Pair;
import io.onedev.server.util.facade.UserFacade;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

import javax.inject.Inject;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;

public abstract class AbstractAuthorizingRealm extends AuthorizingRealm {

    protected final UserManager userManager;
    
    protected final GroupManager groupManager;
    
    protected final ProjectManager projectManager;
    
    protected final SessionManager sessionManager;
    
    protected final SettingManager settingManager;
    
    @SuppressWarnings("serial")
	private static final MetaDataKey<Map<Long, AuthorizationInfo>> AUTHORIZATION_INFOS = new MetaDataKey<>() {};    
    
	@Inject
    public AbstractAuthorizingRealm(UserManager userManager, GroupManager groupManager, 
    		ProjectManager projectManager, SessionManager sessionManager, SettingManager settingManager) {
    	this.userManager = userManager;
    	this.groupManager = groupManager;
    	this.projectManager = projectManager;
    	this.sessionManager = sessionManager;
    	this.settingManager = settingManager;
    }

	private AuthorizationInfo newAuthorizationInfo(Long userId) {
		var result = sessionManager.call(() -> {
			Collection<Permission> permissions = new ArrayList<>();
			User user = null;
			if (userId != 0L) {
				Permission systemAdministration = new SystemAdministration();
				
				user = userManager.load(userId);
				if (user.isRoot() || user.isSystem()) 
					return new Pair<>(user.getFacade(), newArrayList(systemAdministration));
				
				List<Group> groups = new ArrayList<>(user.getGroups());
				Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
				if (defaultLoginGroup != null) 
					groups.add(defaultLoginGroup);

				for (Group group : groups) {
					if (group.implies(systemAdministration))
						return new Pair<>(user.getFacade(), newArrayList(systemAdministration));
					permissions.add(group);
				}
				   
				permissions.add(new UserAdministration(user));
				
				for (UserAuthorization authorization: user.getProjectAuthorizations()) 
					permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
				for (IssueAuthorization authorization: user.getIssueAuthorizations()) {
					permissions.add(new ProjectPermission(
							authorization.getIssue().getProject(), 
							new ConfidentialIssuePermission(authorization.getIssue())));
				}
			}
			if (user != null || settingManager.getSecuritySetting().isEnableAnonymousAccess()) {
				permissions.add(p -> {
					if (p instanceof ProjectPermission) {
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
				});
			}
			
			return new Pair<>(UserFacade.of(user), permissions);
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
				return Lists.newArrayList(permission -> {
					if (permission instanceof BasePermission) {
						BasePermission basePermission = (BasePermission) permission;	
						if (!basePermission.isApplicable(result.getLeft()))							
							return false;
					} 
					return result.getRight().stream().anyMatch(it -> it.implies(permission));
				});
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