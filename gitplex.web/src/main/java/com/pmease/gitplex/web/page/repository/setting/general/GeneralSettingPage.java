package com.pmease.gitplex.web.page.repository.setting.general;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.page.repository.setting.RepoSettingPage;

@SuppressWarnings("serial")
public class GeneralSettingPage extends RepoSettingPage {

	public GeneralSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	protected String getPageTitle() {
		return "General Setting - " + getRepository();
	}

}
