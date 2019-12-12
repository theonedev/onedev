package io.onedev.server.util.init;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.Nullable;

@SuppressWarnings("serial")
public abstract class ManualConfig implements Serializable {

	private final String title;
	
	private final String description;
	
	private final Serializable setting;
	
	private final Collection<String> excludedProperties;
	
	public ManualConfig(String title, @Nullable String description, Serializable setting, 
			Collection<String> excludedProperties) {
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

	public Collection<String> getExcludeProperties() {
		return excludedProperties;
	}

	public abstract Skippable getSkippable();
	
	public abstract void complete();
	
}
