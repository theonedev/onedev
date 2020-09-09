package io.onedev.server.web.component.menu;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.WebMarkupContainer;

public abstract class MenuItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Nullable
	public String getIconHref() {
		return null;
	}
	
	public abstract String getLabel();
	
	public boolean isSelected() {
		return false;
	}
	
	public abstract WebMarkupContainer newLink(String id);
	
	@Nullable
	public String getShortcut() {
		return null;
	};
	
}
