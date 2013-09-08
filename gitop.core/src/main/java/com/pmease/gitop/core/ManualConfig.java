package com.pmease.gitop.core;

import java.io.Serializable;

public abstract class ManualConfig {

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
