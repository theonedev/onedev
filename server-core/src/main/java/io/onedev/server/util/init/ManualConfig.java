package io.onedev.server.util.init;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public abstract class ManualConfig implements Serializable {

	private final String title;
	
	private final String description;
	
	private final Serializable setting;
	
	private final Set<String> excludedProperties;
	
	public ManualConfig(String title, @Nullable String description, Serializable setting, Set<String> excludedProperties) {
		this.title = title;
		this.description = description;
		this.setting = setting;
		this.excludedProperties = excludedProperties;
	}
	
	public ManualConfig(String title, @Nullable String description, Serializable setting) {
		this(title, description, setting, new HashSet<>());
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}

	public Serializable getSetting() {
		return setting;
	}

	public Set<String> getExcludeProperties() {
		return excludedProperties;
	}

	public abstract Skippable getSkippable();
	
	public abstract void complete();
	
}
