package io.onedev.server.web.page.admin.groupmanagement.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupAuthorizationManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.RoleManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.groupmanagement.GroupPage;
import io.onedev.server.web.util.editablebean.ProjectAuthorizationBean;
import io.onedev.server.web.util.editablebean.ProjectAuthorizationsBean;

@SuppressWarnings("serial")
public class GroupAuthorizationsPage extends GroupPage {

	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		ProjectAuthorizationsBean authorizationsBean = new ProjectAuthorizationsBean();
		for (GroupAuthorization authorization: getGroup().getAuthorizations()) {
			ProjectAuthorizationBean authorizationBean = new ProjectAuthorizationBean();
			authorizationBean.setProjectPath(authorization.getProject().getPath());
			authorizationBean.setRoleName(authorization.getRole().getName());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				Set<String> projectPaths = new HashSet<>();
				Collection<GroupAuthorization> authorizations = new ArrayList<>();
				for (ProjectAuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (projectPaths.contains(authorizationBean.getProjectPath())) {
						error("Duplicate authorizations found: " + authorizationBean.getProjectPath());
						return;
					} else {
						projectPaths.add(authorizationBean.getProjectPath());
						GroupAuthorization authorization = new GroupAuthorization();
						authorization.setGroup(getGroup());
						authorization.setProject(OneDev.getInstance(ProjectManager.class).findByPath(authorizationBean.getProjectPath()));
						authorization.setRole(OneDev.getInstance(RoleManager.class).find(authorizationBean.getRoleName()));
						authorizations.add(authorization);
					}
				}
				
				OneDev.getInstance(GroupAuthorizationManager.class).syncAuthorizations(getGroup(), authorizations);
				Session.get().success("Project authorizations updated");
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);
	}

}
