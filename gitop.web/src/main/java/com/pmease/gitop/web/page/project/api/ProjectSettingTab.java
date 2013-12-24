package com.pmease.gitop.web.page.project.api;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.wicket.component.tab.AbstractPageTab;

public class ProjectSettingTab extends AbstractPageTab {
	private static final long serialVersionUID = 1L;

	public ProjectSettingTab(IModel<String> title, Class<? extends Page> pageClass) {
		super(title, pageClass);
	}
	
	public ProjectSettingTab(IModel<String> title,
			Class<? extends Page>[] pageClasses) {
		super(title, pageClasses);
	}

	@Override
	public final String getGroupName() {
		throw new UnsupportedOperationException();
	}

}
