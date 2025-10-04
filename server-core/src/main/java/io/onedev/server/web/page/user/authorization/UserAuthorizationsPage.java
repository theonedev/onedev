package io.onedev.server.web.page.user.authorization;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.RoleService;
import io.onedev.server.service.UserAuthorizationService;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.web.editable.PropertyContext;
import io.onedev.server.web.page.user.UserPage;
import io.onedev.server.web.util.editbean.ProjectAuthorizationBean;
import io.onedev.server.web.util.editbean.ProjectAuthorizationsBean;

public class UserAuthorizationsPage extends UserPage {

	public UserAuthorizationsPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled())
			throw new IllegalStateException();
	}

	private String getAuditContent() {
		var auditData = new TreeMap<String, TreeSet<String>>();
		for (var authorization: getUser().getProjectAuthorizations()) {
			auditData.computeIfAbsent(authorization.getProject().getPath(), k -> new TreeSet<>()).add(authorization.getRole().getName());
		}
		return VersionedXmlDoc.fromBean(auditData).toXML();
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
						error(MessageFormat.format(_T("Duplicate authorizations found: {0}"), authorizationBean.getProjectPath()));
						return;
					} else {
						var project = getProjectService().findByPath(authorizationBean.getProjectPath());
						authorizationBean.getRoleNames().stream().forEach(it -> {
							UserAuthorization authorization = new UserAuthorization();
							authorization.setUser(getUser());
							authorization.setProject(project);
							authorization.setRole(getRoleService().find(it));
							authorizations.add(authorization);
						});
					}
				}
				
				var oldAuditContent = getAuditContent();
				OneDev.getInstance(UserAuthorizationService.class).syncAuthorizations(getUser(), authorizations);
				var newAuditContent = getAuditContent();
				auditService.audit(null, "changed project authorizations in account \"" + getUser().getName() + "\"", oldAuditContent, newAuditContent);

				Session.get().success(_T("Project authorizations updated"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(PropertyContext.edit("editor", authorizationsBean, "authorizations"));
		add(form);	
	}

	private RoleService getRoleService() {
		return OneDev.getInstance(RoleService.class);
	}
	
	private ProjectService getProjectService() {
		return OneDev.getInstance(ProjectService.class);
	}
}
