package io.onedev.server.web.page.project.setting.authorization;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GroupAuthorizationsPage extends ProjectSettingPage {

	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		GroupAuthorizationsBean authorizationsBean = new GroupAuthorizationsBean();
		for (GroupAuthorization authorization: getProject().getGroupAuthorizations()) {
			GroupAuthorizationBean authorizationBean = new GroupAuthorizationBean();
			authorizationBean.setGroupName(authorization.getGroup().getName());
			authorizationBean.setRoleName(authorization.getRole().getName());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				if (getProject().getParent() == null
						|| !SecurityUtils.canManage(getProject().getParent())) {
					boolean canManageProject = false;
					Project project = getProject();
					User user = SecurityUtils.getUser();
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
										&& OneDev.getInstance(RoleManager.class).find(authorizationBean.getRoleName()).isManageProject()) {
									canManageProject = true;													
									break;									
								}
							}
						}
					}
					if (!canManageProject) {
						error("Unable to apply change as otherwise you will not be able to manage this project");
						return;
					}
				}				
				
				Set<String> groupNames = new HashSet<>();
				Collection<GroupAuthorization> authorizations = new ArrayList<>();
				for (GroupAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (groupNames.contains(authorizationBean.getGroupName())) {
						error("Duplicate authorizations found: " + authorizationBean.getGroupName());
						return;
					} else {
						groupNames.add(authorizationBean.getGroupName());
						GroupAuthorization authorization = new GroupAuthorization();
						authorization.setProject(getProject());
						authorization.setGroup(OneDev.getInstance(GroupManager.class).find(authorizationBean.getGroupName()));
						authorization.setRole(OneDev.getInstance(RoleManager.class).find(authorizationBean.getRoleName()));
						authorizations.add(authorization);
					}
				}
				
				OneDev.getInstance(GroupAuthorizationManager.class).syncAuthorizations(getProject(), authorizations);
				Session.get().success("Group authorizations updated");
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Group Authorizations");
	}
	
}
