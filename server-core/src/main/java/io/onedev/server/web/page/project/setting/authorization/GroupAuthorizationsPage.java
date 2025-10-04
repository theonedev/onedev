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
import io.onedev.server.service.GroupAuthorizationService;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.RoleService;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class GroupAuthorizationsPage extends ProjectSettingPage {

	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		GroupAuthorizationsBean authorizationsBean = new GroupAuthorizationsBean();
		var groupRoles = new HashMap<String, List<String>>();		
		for (var authorization: getProject().getGroupAuthorizations()) {
			String groupName = authorization.getGroup().getName();
			String roleName = authorization.getRole().getName();			
			groupRoles.computeIfAbsent(groupName, k -> new ArrayList<>()).add(roleName);
		}
		for (var entry: groupRoles.entrySet()) {
			GroupAuthorizationBean authorizationBean = new GroupAuthorizationBean();
			authorizationBean.setGroupName(entry.getKey());
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
						for (var authorization: project.getUserAuthorizations()) {
							if (authorization.getUser().equals(user) 
									&& authorization.getRole().isManageProject()) {
								canManageProject = true;
								break;
							}
						}
						if (!canManageProject && user.getGroups().stream().anyMatch(it->it.isAdministrator()))
							canManageProject = true;
						if (!canManageProject) {
							for (GroupAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
								if (user.getGroups().stream().anyMatch(it->it.getName().equals(authorizationBean.getGroupName()))
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
				
				Set<String> groupNames = new HashSet<>();
				Collection<GroupAuthorization> authorizations = new ArrayList<>();
				for (GroupAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (!groupNames.add(authorizationBean.getGroupName())) {
						error(_T("Duplicate authorizations found: ") + authorizationBean.getGroupName());
						return;
					} else {
						var group = getGroupService().find(authorizationBean.getGroupName());
						authorizationBean.getRoleNames().stream().forEach(it -> {
							GroupAuthorization authorization = new GroupAuthorization();
							authorization.setProject(getProject());
							authorization.setGroup(group);
							authorization.setRole(getRoleService().find(it));
							authorizations.add(authorization);
						});
					}
				}
				
				var oldAuditContent = getAuditContent();
				getGroupAuthorizationService().syncAuthorizations(getProject(), authorizations);
				var newAuditContent = getAuditContent();
				auditService.audit(getProject(), "changed group authorizations", oldAuditContent, newAuditContent);

				Session.get().success(_T("Group authorizations updated"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);
	}

	private String getAuditContent() {
		var auditData = new TreeMap<String, TreeSet<String>>();
		for (var authorization: getProject().getGroupAuthorizations()) {
			auditData.computeIfAbsent(authorization.getGroup().getName(), k -> new TreeSet<>()).add(authorization.getRole().getName());
		}
		return VersionedXmlDoc.fromBean(auditData).toXML();
	}

	private RoleService getRoleService() {
		return OneDev.getInstance(RoleService.class);
	}
	
	private GroupService getGroupService() {
		return OneDev.getInstance(GroupService.class);
	}

	private GroupAuthorizationService getGroupAuthorizationService() {
		return OneDev.getInstance(GroupAuthorizationService.class);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Group Authorizations"));
	}
	
}
