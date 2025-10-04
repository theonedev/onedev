package io.onedev.server.web.page.project.setting.code.git;

import static io.onedev.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.ProjectService;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class GitPackConfigPage extends ProjectSettingPage {

	public GitPackConfigPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		GitPackConfig bean = getProject().getGitPackConfig();
		var oldAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				var newAuditContent = VersionedXmlDoc.fromBean(bean).toXML();
				getProject().setGitPackConfig(bean);
				var projectService = OneDev.getInstance(ProjectService.class);
				var clusterService = OneDev.getInstance(ClusterService.class);
				projectService.update(getProject());
				auditService.audit(getProject(), "changed git pack config", oldAuditContent, newAuditContent);

				Long projectId = getProject().getId();
				GitPackConfig gitPackConfig = getProject().getGitPackConfig();
				String activeServer = projectService.getActiveServer(projectId, false);
				if (activeServer != null) {
					clusterService.runOnServer(activeServer, new ClusterTask<Void>() {

						@Override
						public Void call() throws Exception {
							projectService.checkGitConfig(projectId, gitPackConfig);
							return null;
						}

					});
				}
				
				setResponsePage(GitPackConfigPage.class, GitPackConfigPage.paramsOf(getProject()));
				Session.get().success(_T("Git pack config updated"));
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>" + _T("Git Pack Config") + "</span>").setEscapeModelStrings(false);
	}

	@Override
	protected BookmarkablePageLink<Void> navToProject(String componentId, Project project) {
		if (SecurityUtils.canManageProject(project)) 
			return new ViewStateAwarePageLink<Void>(componentId, GitPackConfigPage.class, paramsOf(project.getId()));
		else 
			return new ViewStateAwarePageLink<Void>(componentId, ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
	}
	
}
