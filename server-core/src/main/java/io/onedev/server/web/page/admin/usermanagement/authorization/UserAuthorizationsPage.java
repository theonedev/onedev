package io.onedev.server.web.page.admin.usermanagement.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.usermanagement.UserPage;
import io.onedev.server.web.util.editbean.ProjectAuthorizationBean;
import io.onedev.server.web.util.editbean.ProjectAuthorizationsBean;

public class UserAuthorizationsPage extends UserPage {

	public UserAuthorizationsPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled())
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ProjectAuthorizationsBean authorizationsBean = new ProjectAuthorizationsBean();
		var userRoles = new HashMap<String, List<String>>();		
		for (var authorization: getUser().getProjectAuthorizations()) {
			String projectPath = authorization.getProject().getPath();
			String roleName = authorization.getRole().getName();			
			userRoles.computeIfAbsent(projectPath, k -> new ArrayList<>()).add(roleName);
		}
		for (var entry: userRoles.entrySet()) {
			ProjectAuthorizationBean authorizationBean = new ProjectAuthorizationBean();
			authorizationBean.setProjectPath(entry.getKey());
			authorizationBean.setRoleNames(entry.getValue());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				Set<String> projectNames = new HashSet<>();
				Collection<UserAuthorization> authorizations = new ArrayList<>();
				for (ProjectAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (!projectNames.add(authorizationBean.getProjectPath())) {
						error("Duplicate authorizations found: " + authorizationBean.getProjectPath());
						return;
					} else {
						var project = getProjectManager().findByPath(authorizationBean.getProjectPath());
						authorizationBean.getRoleNames().stream().forEach(it -> {
							UserAuthorization authorization = new UserAuthorization();
							authorization.setUser(getUser());
							authorization.setProject(project);
							authorization.setRole(getRoleManager().find(it));
							authorizations.add(authorization);
						});
					}
				}
				
				OneDev.getInstance(UserAuthorizationManager.class).syncAuthorizations(getUser(), authorizations);
				Session.get().success("Project authorizations updated");
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);	
	}

	private RoleManager getRoleManager() {
		return OneDev.getInstance(RoleManager.class);
	}
	
	private ProjectManager getProjectManager() {
		return OneDev.getInstance(ProjectManager.class);
	}
}
