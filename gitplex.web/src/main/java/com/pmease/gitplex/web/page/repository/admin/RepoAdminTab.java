package com.pmease.gitplex.web.page.repository.admin;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;

@SuppressWarnings("serial")
public class RepoAdminTab extends PageTab {

	public RepoAdminTab(IModel<String> titleModel, Class<? extends RepoAdminPage> pageClass) {
		super(titleModel, pageClass);
	}

	public RepoAdminTab(IModel<String> titleModel, Class<? extends RepoAdminPage> pageClass, 
			Class<? extends RepoAdminPage> additionalPageClass) {
		super(titleModel, pageClass, additionalPageClass);
	}

	@Override
	public Component render(String componentId) {
		return new RepoAdminTabLink(componentId, this);
	}

}
