package com.gitplex.server.web.page.project;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.gitplex.server.web.page.project.blob.ProjectBlobPage;

@SuppressWarnings("serial")
public class NoCommitsPage extends ProjectPage {

	public NoCommitsPage(PageParameters params) {
		super(params);
		
		if (getProject().getDefaultBranch() != null)
			throw new RestartResponseException(ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("url1", getProject().getUrl()));
		add(new Label("url2", getProject().getUrl()));
		add(new Label("url3", getProject().getUrl()));
	}

}
