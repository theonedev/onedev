package io.onedev.server.web.page.admin.group.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupAuthorizationManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.admin.AuthorizationBean;
import io.onedev.server.web.page.admin.AuthorizationsBean;
import io.onedev.server.web.page.admin.group.GroupPage;

@SuppressWarnings("serial")
public class GroupAuthorizationsPage extends GroupPage {

	public GroupAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		AuthorizationsBean authorizationsBean = new AuthorizationsBean();
		for (GroupAuthorization authorization: getGroup().getAuthorizations()) {
			AuthorizationBean authorizationBean = new AuthorizationBean();
			authorizationBean.setProjectName(authorization.getProject().getName());
			authorizationBean.setRoleName(authorization.getRole().getName());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				Set<String> projectNames = new HashSet<>();
				Collection<GroupAuthorization> authorizations = new ArrayList<>();
				for (AuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (projectNames.contains(authorizationBean.getProjectName())) {
						error("Duplicate authorizations found: " + authorizationBean.getProjectName());
						return;
					} else {
						projectNames.add(authorizationBean.getProjectName());
						GroupAuthorization authorization = new GroupAuthorization();
						authorization.setGroup(getGroup());
						authorization.setProject(OneDev.getInstance(ProjectManager.class).find(authorizationBean.getProjectName()));
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
