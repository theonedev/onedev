package com.pmease.gitop.web.common.wicket.component.tab;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

public abstract class AbstractPageTab extends AbstractGroupTab implements IPageTab {
	private static final long serialVersionUID = 1L;

	protected final Class<? extends Page>[] pageClasses;
	
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

	@Override
	public WebMarkupContainer getPanel(String panelId) {
		throw new UnsupportedOperationException();
	}
}
