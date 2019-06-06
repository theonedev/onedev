package io.onedev.server.web.page.project;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.issues.list.IssueListPage;

@SuppressWarnings("serial")
public class NoCommitsPage extends ProjectPage {

	public NoCommitsPage(PageParameters params) {
		super(params);
		
		if (getProject().getDefaultBranch() != null) {
			if (SecurityUtils.canReadCode(getProject().getFacade()))
				throw new RestartResponseException(ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject()));
			else
				throw new RestartResponseException(IssueListPage.class, ProjectBlobPage.paramsOf(getProject()));
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("url1", getProject().getUrl()));
		add(new Label("url2", getProject().getUrl()));
		add(new Label("url3", getProject().getUrl()));
	}

}
