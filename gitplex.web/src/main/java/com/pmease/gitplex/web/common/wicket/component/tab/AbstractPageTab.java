package com.pmease.gitplex.web.common.wicket.component.tab;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.pmease.gitplex.web.common.wicket.component.link.LinkPanel;

public abstract class AbstractPageTab extends AbstractGroupTab implements IPageTab {
	private static final long serialVersionUID = 1L;

	protected final Class<? extends Page>[] pageClasses;
	
	@SuppressWarnings("unchecked")
	public AbstractPageTab(IModel<String> title, final Class<? extends Page> pageClass) {
		this(title, new Class[] { pageClass });
	}
	
	public AbstractPageTab(IModel<String> title, final Class<? extends Page>[] pageClasses) {
		super(title);
		this.pageClasses = pageClasses;
	}

	@Override
	public boolean isSelected(Page page) {
		for (Class<? extends Page> each : pageClasses) {
			if (each.isAssignableFrom(page.getClass())) {
				return true;
			}
		}
		
		return false;
	}

	public Component newTabLink(String id, final PageParameters params) {
		return new LinkPanel(id, getTitle()) {
			private static final long serialVersionUID = 1L;

			@Override
			protected AbstractLink createLink(String id) {
				return new BookmarkablePageLink<Void>(id, getBookmarkablePageClass(), params);
			}
		};
	}
	
	public Class<? extends Page> getBookmarkablePageClass() {
		return pageClasses[0];
	}

	@Override
	public WebMarkupContainer getPanel(String panelId) {
		throw new UnsupportedOperationException();
	}
}
