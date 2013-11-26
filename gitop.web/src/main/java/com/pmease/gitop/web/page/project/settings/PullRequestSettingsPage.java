package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class PullRequestSettingsPage extends AbstractProjectSettingPage {

	public PullRequestSettingsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.PULL_REQUESTS;
	}

}
