package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class ProjectPermissionsPage extends AbstractProjectSettingPage {

	public ProjectPermissionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.PERMISSIONS;
	}

}
