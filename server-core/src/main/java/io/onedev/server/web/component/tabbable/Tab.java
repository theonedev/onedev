package io.onedev.server.web.component.tabbable;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.Component;

public abstract class Tab implements Serializable {
	
	protected abstract Component render(String id);
	
	@Nullable
	protected Component renderOptions(String id) {
		return null;
	}
	
	public abstract boolean isSelected();
	
	public abstract String getTitle();
	
}
