package com.pmease.gitplex.web.page.repository;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class RepositoryTab extends PageTab {

	private final String iconClass;
	
	public RepositoryTab(IModel<String> titleModel, String iconClass, Class<? extends RepositoryPage> pageClass) {
		super(titleModel, pageClass);
		
		this.iconClass = iconClass;
	}

	public RepositoryTab(IModel<String> titleModel, String iconClass, Class<? extends RepositoryPage> pageClass, 
			Class<? extends RepositoryPage> additionalPageClass) {
		super(titleModel, pageClass, additionalPageClass);
		
		this.iconClass = iconClass;
	}

	@Override
	public Component render(String componentId) {
		return new RepoTabLink(componentId, this);
	}
	
	public String getIconClass() {
		return iconClass;
	}
}
