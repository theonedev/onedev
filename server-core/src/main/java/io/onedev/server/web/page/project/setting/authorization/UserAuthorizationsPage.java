package io.onedev.server.web.page.project.setting.authorization;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.UserAuthorizationService;
import io.onedev.server.service.UserService;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class UserAuthorizationsPage extends ProjectSettingPage {

	public UserAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		UserAuthorizationsBean authorizationsBean = new UserAuthorizationsBean();
		var userRoles = new HashMap<String, List<String>>();		
		for (var authorization: getProject().getUserAuthorizations()) {
			String userName = authorization.getUser().getName();
			String roleName = authorization.getRole().getName();			
			userRoles.computeIfAbsent(userName, k -> new ArrayList<>()).add(roleName);
		}
		for (var entry: userRoles.entrySet()) {
			UserAuthorizationBean authorizationBean = new UserAuthorizationBean();
			authorizationBean.setUserName(entry.getKey());
			authorizationBean.setRoleNames(entry.getValue());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				SecurityUtils.checkPermission(new ProjectPermission(getProject(), new ManageProject()));
				
				if (getProject().getParent() == null 
						|| !SecurityUtils.canManageProject(getProject().getParent())) {
					boolean canManageProject = false;
					Project project = getProject();
					User user = SecurityUtils.getAuthUser();
					if (user.isRoot()) {
						canManageProject = true;
					} else {
						for (Membership membership: user.getMemberships()) {
							if (membership.getGroup().isAdministrator()) {
								canManageProject = true;
							} else {
								for (GroupAuthorization authorization: membership.getGroup().getAuthorizations()) {
									if (authorization.getProject().equals(project) && authorization.getRole().isManageProject()) {
										canManageProject = true;
										break;
									}
								}
							}
							if (canManageProject)
								break;
						}
						if (!canManageProject) {
							for (UserAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
								if (authorizationBean.getUserName().equals(user.getName())
										&& authorizationBean.getRoleNames().stream().anyMatch(it -> getRoleService().find(it).isManageProject())) {
									canManageProject = true;
									break;
								}
							}
						}
					}
					if (!canManageProject) {
						error(_T("Unable to apply change as otherwise you will not be able to manage this project"));
						return;
					}
				}
				Set<String> userNames = new HashSet<>();
				Collection<UserAuthorization> authorizations = new ArrayList<>();
				for (UserAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (!userNames.add(authorizationBean.getUserName())) {
						error(_T("Duplicate authorizations found: ") + authorizationBean.getUserName());
						return;
					} else {
						var user = getUserService().findByName(authorizationBean.getUserName());
						authorizationBean.getRoleNames().stream().forEach(it -> {
							UserAuthorization authorization = new UserAuthorization();
							authorization.setProject(getProject());
							authorization.setUser(user);
							authorization.setRole(getRoleService().find(it));
							authorizations.add(authorization);
						});
					}
				}
				
				var oldAuditContent = getAuditContent();
				getUserAuthorizationService().syncAuthorizations(getProject(), authorizations);

				var newAuditContent = getAuditContent();
				auditService.audit(getProject(), "changed user authorizations", oldAuditContent, newAuditContent);

				Session.get().success(_T("User authorizations updated"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);
	}

	private String getAuditContent() {
		var auditData = new TreeMap<String, TreeSet<String>>();
		for (var authorization: getProject().getUserAuthorizations()) {
			auditData.computeIfAbsent(authorization.getUser().getName(), k -> new TreeSet<>()).add(authorization.getRole().getName());
		}
		return VersionedXmlDoc.fromBean(auditData).toXML();
	}

	private RoleService getRoleService() {
		return OneDev.getInstance(RoleService.class);
	}

	private UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}

	private UserAuthorizationService getUserAuthorizationService() {
		return OneDev.getInstance(UserAuthorizationService.class);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("User Authorizations"));
	}
	
}
