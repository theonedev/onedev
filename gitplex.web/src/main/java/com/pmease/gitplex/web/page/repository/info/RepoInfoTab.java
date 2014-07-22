package com.pmease.gitplex.web.page.repository.info;

import com.pmease.gitplex.web.page.repository.RepositoryPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import com.pmease.commons.wicket.component.tabbable.PageTab;
import com.pmease.commons.wicket.component.tabbable.Tab;

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
	public void populate(ListItem<Tab> item, String componentId) {
		item.add(new RepoInfoTabLink(componentId, (RepoInfoTab) item.getModelObject()));
	}
	
	public String getIconClass() {
		return iconClass;
	}
}
