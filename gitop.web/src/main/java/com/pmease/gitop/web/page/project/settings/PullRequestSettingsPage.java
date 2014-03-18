package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class PullRequestSettingsPage extends AbstractRepositorySettingPage {

	public PullRequestSettingsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected String getPageTitle() {
		return "Pull Request Settings - " + getProject();
	}

}
