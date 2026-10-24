package io.onedev.server.web.component.menu;

import java.io.Serial;
import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.markup.html.WebMarkupContainer;

public abstract class MenuItem implements Serializable {
	
	@Serial
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
