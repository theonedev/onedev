package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class ProjectHooksPage extends AbstractProjectSettingPage {

	public ProjectHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.HOOKS;
	}

}
