package com.pmease.gitplex.web.page.repository.info;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.gitplex.web.page.repository.RepositoryPage;

@SuppressWarnings("serial")
public class RepoInfoTab extends PageTab {

	private final String iconClass;
	
	public RepoInfoTab(IModel<String> titleModel, String iconClass, Class<? extends RepositoryInfoPage> pageClass) {
		super(titleModel, pageClass);
		
		this.iconClass = iconClass;
	}

	public RepoInfoTab(IModel<String> titleModel, String iconClass, Class<? extends RepositoryInfoPage> pageClass, 
			Class<? extends RepositoryPage> additionalPageClass) {
		super(titleModel, pageClass, additionalPageClass);
		
		this.iconClass = iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new RepoInfoTabLink(componentId, this);
	}
	
	public String getIconClass() {
		return iconClass;
	}
}
