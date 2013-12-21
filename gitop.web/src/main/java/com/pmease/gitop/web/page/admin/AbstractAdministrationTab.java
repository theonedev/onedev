package com.pmease.gitop.web.page.admin;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;

import com.pmease.gitop.web.common.wicket.component.link.LinkPanel;
import com.pmease.gitop.web.page.admin.api.IAdministrationTab;
import com.pmease.gitop.web.util.WicketUtils;

public abstract class AbstractAdministrationTab extends AbstractTab implements IAdministrationTab {
	private static final long serialVersionUID = 1L;
	
	private final Category category;
	
	public AbstractAdministrationTab(IModel<String> title,  Category category) {
		super(title);
		this.category = category;
	}

	@Override
	public Category getCategory() {
		return category;
	}

	@SuppressWarnings("serial")
	@Override
	public Component newTabLink(String id) {
		return new LinkPanel(id, getTitle()) {

			@Override
			protected AbstractLink createLink(String id) {
				return new BookmarkablePageLink<Void>(id, AdministrationPage.class,
						WicketUtils.newPageParams("tabId", getTabId()));
			}
			
		};
	}
	
	@Override
	public String getTabId() {
		return getTitle().getObject().replace(" ", "-").toLowerCase();
	}
}
