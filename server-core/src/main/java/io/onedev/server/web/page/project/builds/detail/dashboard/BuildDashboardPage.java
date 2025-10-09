package io.onedev.server.web.page.project.builds.detail.dashboard;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.artifacts.BuildArtifactsPage;
import io.onedev.server.web.page.project.builds.detail.issues.FixedIssuesPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;
import io.onedev.server.web.page.project.builds.detail.pipeline.BuildPipelinePage;

public class BuildDashboardPage extends BuildDetailPage {

	public BuildDashboardPage(PageParameters params) {
		super(params);
		
		PageProvider pageProvider;
		
		if (getBuild().getRootArtifacts().size() != 0) {
			pageProvider = new PageProvider(BuildArtifactsPage.class, BuildArtifactsPage.paramsOf(getBuild()));
		} else if (SecurityUtils.canAccessLog(getBuild())) {
			pageProvider = new PageProvider(BuildLogPage.class, BuildLogPage.paramsOf(getBuild()));
		} else if (SecurityUtils.canAccessPipeline(getBuild())) {
			pageProvider = new PageProvider(BuildPipelinePage.class, BuildPipelinePage.paramsOf(getBuild()));
		} else {
			pageProvider = new PageProvider(FixedIssuesPage.class, FixedIssuesPage.paramsOf(getBuild()));
		}
		
		throw new RestartResponseException(pageProvider, RedirectPolicy.NEVER_REDIRECT);
	}

}
