package io.onedev.server.web.component.tabbable;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

@SuppressWarnings("serial")
public abstract class Tab implements Serializable {
	
	protected abstract Component render(String componentId);
	
	@Nullable
	protected Component renderOptions(String componentId) {
		return null;
	}
	
	public abstract boolean isSelected();
	
	public abstract String getTitle();
}
