package io.onedev.server.web.page.project.setting.code.git;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.code.GitPackConfig;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.UUID;

@SuppressWarnings("serial")
public class GitPackConfigPage extends ProjectSettingPage {

	public GitPackConfigPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		GitPackConfig bean = getProject().getGitPackConfig();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				
				getProject().setGitPackConfig(bean);
				var projectManager = OneDev.getInstance(ProjectManager.class);
				var clusterManager = OneDev.getInstance(ClusterManager.class);
				projectManager.save(getProject());

				Long projectId = getProject().getId();
				GitPackConfig gitPackConfig = getProject().getGitPackConfig();
				UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, false);
				if (storageServerUUID != null) {
					clusterManager.runOnServer(storageServerUUID, new ClusterTask<Void>() {

						@Override
						public Void call() throws Exception {
							projectManager.checkGitConfig(projectId, gitPackConfig);
							return null;
						}

					});
				}
				
				setResponsePage(GitPackConfigPage.class, GitPackConfigPage.paramsOf(getProject()));
				Session.get().success("Git pack config updated");
			}
			
		};
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Git Pack Config</span>").setEscapeModelStrings(false);
	}

	@Override
	protected void navToProject(Project project) {
		if (SecurityUtils.canManage(project)) 
			setResponsePage(GitPackConfigPage.class, paramsOf(project.getId()));
		else 
			setResponsePage(ProjectDashboardPage.class, ProjectPage.paramsOf(project.getId()));
	}
	
}
