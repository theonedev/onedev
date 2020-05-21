package io.onedev.server.web.page.project.builds.detail.dashboard;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.builds.detail.BuildDetailPage;
import io.onedev.server.web.page.project.builds.detail.artifacts.BuildArtifactsPage;
import io.onedev.server.web.page.project.builds.detail.issues.FixedIssuesPage;
import io.onedev.server.web.page.project.builds.detail.log.BuildLogPage;

@SuppressWarnings("serial")
public class BuildDashboardPage extends BuildDetailPage {

	public BuildDashboardPage(PageParameters params) {
		super(params);
		
		if (SecurityUtils.canAccessLog(getBuild()))
			throw new RestartResponseException(BuildLogPage.class, BuildLogPage.paramsOf(getBuild()));
		else if (getBuild().getArtifactsDir().exists())
			throw new RestartResponseException(BuildArtifactsPage.class, BuildArtifactsPage.paramsOf(getBuild()));
		else
			throw new RestartResponseException(FixedIssuesPage.class, FixedIssuesPage.paramsOf(getBuild()));
		
	}

}
