package com.pmease.commons.util.init;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class ManualConfig implements Serializable {

	private final String message;
	
	private final Serializable setting;
	
	public ManualConfig(String message, Serializable setting) {
		this.message = message;
		this.setting = setting;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Serializable getSetting() {
		return setting;
	}

	public abstract Skippable getSkippable();
	
	public abstract void complete();
	
}
