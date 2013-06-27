package com.pmease.commons.web.behavior.menu;

import java.io.Serializable;

import org.apache.wicket.Component;

public abstract class MenuItem implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract Component newContent(String componentId);
}
