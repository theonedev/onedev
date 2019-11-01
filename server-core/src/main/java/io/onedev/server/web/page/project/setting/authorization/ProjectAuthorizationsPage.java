package io.onedev.server.web.page.project.setting.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.RoleManager;
import io.onedev.server.entitymanager.UserAuthorizationManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

@SuppressWarnings("serial")
public class ProjectAuthorizationsPage extends ProjectSettingPage {

	public ProjectAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		AuthorizationsBean authorizationsBean = new AuthorizationsBean();
		for (UserAuthorization authorization: getProject().getUserAuthorizations()) {
			AuthorizationBean authorizationBean = new AuthorizationBean();
			authorizationBean.setUserName(authorization.getUser().getName());
			authorizationBean.setRoleName(authorization.getRole().getName());
			authorizationsBean.getAuthorizations().add(authorizationBean);
		}

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();

				Set<String> userNames = new HashSet<>();
				Collection<UserAuthorization> authorizations = new ArrayList<>();
				for (AuthorizationBean authorizationBean: authorizationsBean.getAuthorizations()) {
					if (userNames.contains(authorizationBean.getUserName())) {
						error("Duplicate authorizations found: " + authorizationBean.getUserName());
						return;
					} else {
						userNames.add(authorizationBean.getUserName());
						UserAuthorization authorization = new UserAuthorization();
						authorization.setProject(getProject());
						authorization.setUser(OneDev.getInstance(UserManager.class).findByName(authorizationBean.getUserName()));
						authorization.setRole(OneDev.getInstance(RoleManager.class).find(authorizationBean.getRoleName()));
						authorizations.add(authorization);
					}
				}
				
				OneDev.getInstance(UserAuthorizationManager.class).authorize(getProject(), authorizations);
				Session.get().success("User authorizations updated");
			}
			
		};
		form.add(new NotificationPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);
	}
}
