package com.pmease.commons.util.init;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class ManualConfig implements Serializable {

	private final Serializable setting;
	
	public ManualConfig(Serializable setting) {
		this.setting = setting;
	}
	
	public Serializable getSetting() {
		return setting;
	}

	public abstract Skippable getSkippable();
	
	public abstract void complete();
	
}
