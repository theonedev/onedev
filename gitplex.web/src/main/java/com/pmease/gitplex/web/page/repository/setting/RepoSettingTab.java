package com.pmease.gitplex.web.page.repository.setting;

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoSettingTab extends PageTab {

	private final String iconClass;
	
	public RepoSettingTab(String title, String iconClass, Class<? extends RepoSettingPage> pageClass) {
		super(Model.of(title), pageClass);
		
		this.iconClass = iconClass;
	}

	public RepoSettingTab(String title, String iconClass, Class<? extends RepoSettingPage> pageClass, 
			Class<? extends RepositoryPage> additionalPageClass) {
		super(Model.of(title), pageClass, additionalPageClass);
		
		this.iconClass = iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new RepoSettingTabLink(componentId, this);
	}
	
	public String getIconClass() {
		return iconClass;
	}
}
