package com.pmease.commons.wicket.component.menu;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.markup.html.link.AbstractLink;

public abstract class MenuItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Nullable
	public abstract String getIconClass();
	
	public abstract String getLabel();
	
	public abstract AbstractLink newLink(String id);
	
}
