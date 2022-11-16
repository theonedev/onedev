package io.onedev.server.web.page.project.builds.detail.dashboard;

import java.io.File;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.artifacts.BuildArtifactsPage;
import io.onedev.server.web.page.project.builds.detail.issues.FixedIssuesPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;
import io.onedev.server.web.page.project.builds.detail.pipeline.BuildPipelinePage;

@SuppressWarnings("serial")
public class BuildDashboardPage extends BuildDetailPage {

	public BuildDashboardPage(PageParameters params) {
		super(params);
		
		PageProvider pageProvider;
		if (SecurityUtils.canAccessLog(getBuild())) {
			pageProvider = new PageProvider(BuildLogPage.class, BuildLogPage.paramsOf(getBuild()));
		} else if (SecurityUtils.canReadCode(getProject())) {
			pageProvider = new PageProvider(BuildPipelinePage.class, BuildPipelinePage.paramsOf(getBuild()));
		} else {
			Long projectId = getBuild().getProject().getId();
			Long buildNumber = getBuild().getNumber();
			
			boolean hasArtifacts = OneDev.getInstance(ProjectManager.class).runOnProjectServer(projectId, new ClusterTask<Boolean>() {

				@Override
				public Boolean call() throws Exception {
					File artifactsDir = Build.getArtifactsDir(projectId, buildNumber);
					return artifactsDir.exists() && artifactsDir.listFiles().length != 0;
				}
				
			});
			if (hasArtifacts)
				pageProvider = new PageProvider(BuildArtifactsPage.class, BuildArtifactsPage.paramsOf(getBuild()));
			else
				pageProvider = new PageProvider(FixedIssuesPage.class, FixedIssuesPage.paramsOf(getBuild()));
		}
		
		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

}
