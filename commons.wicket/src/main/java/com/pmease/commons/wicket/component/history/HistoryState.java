package com.pmease.commons.wicket.component.history;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HistoryState implements Serializable {
	
	private final String componentPath;
	
	private final Serializable customObj;
	
	public HistoryState(String componentPath, Serializable customObj) {
		this.componentPath = componentPath;
		this.customObj = customObj;
	}

	public String getComponentPath() {
		return componentPath;
	}

	public Serializable getCustomObj() {
		return customObj;
	}
	
}
