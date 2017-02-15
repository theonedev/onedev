package com.gitplex.server.web.component.tabbable;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.list.ListItem;

import com.google.common.base.Preconditions;

@SuppressWarnings("serial")
public abstract class Tab implements Serializable {
	
	private ListItem<Tab> item;
	
	void setItem(ListItem<Tab> item) {
		this.item = item;
	}
	
	protected ListItem<Tab> getItem() {
		Preconditions.checkNotNull(item);
		return item;
	}
	
	protected abstract Component render(String componentId);
	
	public abstract boolean isSelected();
}
