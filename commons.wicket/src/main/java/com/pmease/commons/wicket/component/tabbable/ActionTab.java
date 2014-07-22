package com.pmease.commons.wicket.component.tabbable;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

public abstract class ActionTab implements Tab {

	private static final long serialVersionUID = 1L;

	private IModel<String> titleModel;
	
	private boolean active;
	
	public ActionTab(IModel<String> titleModel) {
		this.titleModel = titleModel;
	}
	
	protected final IModel<String> getTitleModel() {
		return titleModel;
	}
	
	@Override
	public void populate(ListItem<Tab> item, String componentId) {
		item.add(new ActionTabLink(componentId, this));
	}

	public final ActionTab setActive(boolean active) {
		this.active = active;
		return this;
	}

	@Override
	public final boolean isActive(ListItem<Tab> item) {
		return active;
	}

	protected abstract void tabActivated();
}
