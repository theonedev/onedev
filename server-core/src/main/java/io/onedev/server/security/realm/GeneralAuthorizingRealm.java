package io.onedev.server.security.realm;

import static io.onedev.server.security.SecurityUtils.getUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

import com.google.common.collect.Lists;

import io.onedev.server.service.GroupService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.security.permission.ConfidentialIssuePermission;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.security.permission.UserAdministration;
import io.onedev.server.util.Pair;
import io.onedev.server.util.facade.UserFacade;

public class GeneralAuthorizingRealm extends AuthorizingRealm {

    protected final UserService userService;
    
    protected final GroupService groupService;
    
    protected final ProjectService projectService;
    
    protected final SessionService sessionService;
    
    protected final SettingService settingService;
    
	private static final MetaDataKey<Map<String, AuthorizationInfo>> AUTHORIZATION_INFOS = new MetaDataKey<>() {};    
    
	@Inject
    public GeneralAuthorizingRealm(UserService userService, GroupService groupService,
                                   ProjectService projectService, SessionService sessionService, SettingService settingService) {
    	this.userService = userService;
    	this.groupService = groupService;
    	this.projectService = projectService;
    	this.sessionService = sessionService;
    	this.settingService = settingService;
    }
	
	private AuthorizationInfo newAuthorizationInfo(String principal) {
		var result = sessionService.call(() -> {
			Collection<Permission> permissions = new ArrayList<>();
			var user = SecurityUtils.getAuthUser(principal);
			if (user != null) {
				boolean isAdmin = false;
				var systemAdministration = new SystemAdministration();
				if (user.isSystem() || user.isRoot()) {
					permissions.add(systemAdministration);
					isAdmin = true;
				}

				if (!isAdmin) {
					for (Group group : user.getGroups()) {
						if (group.implies(systemAdministration)) {
							permissions.add(systemAdministration);
							isAdmin = true;
							break;
						}
						permissions.add(group);
					}
				}

				if (!isAdmin) {
					permissions.add(new UserAdministration(user));

					for (UserAuthorization authorization : user.getProjectAuthorizations())
						permissions.add(new ProjectPermission(authorization.getProject(), authorization.getRole()));
					for (IssueAuthorization authorization : user.getIssueAuthorizations()) {
						permissions.add(new ProjectPermission(
								authorization.getIssue().getProject(),
								new ConfidentialIssuePermission(authorization.getIssue())));
					}
				}
			}
			
			var accessToken = SecurityUtils.getAccessToken(principal);
			if (accessToken != null) {
				var owner = accessToken.getOwner();
				for (var authorization: accessToken.getAuthorizations()) {
					var project = authorization.getProject();
					if (SecurityUtils.canManageProject(owner.asSubject(), project)) 
						permissions.add(new ProjectPermission(project, authorization.getRole()));
				}
			}
			
			if (!SecurityUtils.isAnonymous(principal) || settingService.getSecuritySetting().isEnableAnonymousAccess()) {
				permissions.add(p -> {
					if (p instanceof ProjectPermission) {
						ProjectPermission projectPermission = (ProjectPermission) p;
						Project project = projectPermission.getProject();
						Permission privilege = projectPermission.getPrivilege();
						do {
							if (project.isPermittedByLoginUser(privilege))
								return true;
							project = project.getParent();
						} while (project != null);
					}
					return false;
				});
			}
			return new Pair<>(UserFacade.of(getUser(user, accessToken)), permissions);
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
		String principal = (String) principals.getPrimaryPrincipal();
		RequestCycle requestCycle = RequestCycle.get();
		if (requestCycle != null) {
			Map<String, AuthorizationInfo> authorizationInfos = requestCycle.getMetaData(AUTHORIZATION_INFOS);
			if (authorizationInfos == null) {
				authorizationInfos = new HashMap<>();
				requestCycle.setMetaData(AUTHORIZATION_INFOS, authorizationInfos);
			}
			AuthorizationInfo authorizationInfo = authorizationInfos.get(principal);
			if (authorizationInfo == null) {
				authorizationInfo = newAuthorizationInfo(principal);
				authorizationInfos.put(principal, authorizationInfo);
			}
			return authorizationInfo;
		} else {
			return newAuthorizationInfo(principal);
		}
	}
	
	@Override
	public boolean supports(AuthenticationToken token) {
		return false;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		throw new UnsupportedOperationException();
	}
	
}