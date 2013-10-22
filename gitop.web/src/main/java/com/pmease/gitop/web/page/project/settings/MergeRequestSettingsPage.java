package com.pmease.gitop.web.page.project.settings;

import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public class MergeRequestSettingsPage extends AbstractProjectSettingPage {

	public MergeRequestSettingsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Category getCategory() {
		return Category.MERGE_REQUESTS;
	}

}
