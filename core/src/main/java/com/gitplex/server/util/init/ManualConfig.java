package com.gitplex.server.util.init;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public abstract class ManualConfig implements Serializable {

	private final String message;
	
	private final Serializable setting;
	
	private final Set<String> excludeProperties;
	
	public ManualConfig(String message, Serializable setting, Set<String> excludeProperties) {
		this.message = message;
		this.setting = setting;
		this.excludeProperties = excludeProperties;
	}
	
	public ManualConfig(String message, Serializable setting) {
		this(message, setting, new HashSet<>());
	}
	
	public String getMessage() {
		return message;
	}
	
	public Serializable getSetting() {
		return setting;
	}

	public Set<String> getExcludeProperties() {
		return excludeProperties;
	}

	public abstract Skippable getSkippable();
	
	public abstract void complete();
	
}
