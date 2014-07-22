package com.pmease.gitplex.web.page.repository.admin;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tab;

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
	public void populate(ListItem<Tab> item, String componentId) {
		item.add(new RepoAdminTabLink(componentId, (RepoAdminTab) item.getModelObject()));
	}

}
