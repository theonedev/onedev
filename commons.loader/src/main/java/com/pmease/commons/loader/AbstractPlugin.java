package com.pmease.commons.loader;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractPlugin implements Plugin {
	
	private String id;
	
	private String name;
	
	private String vendor;
	
	private String version;
	
	private String description;
	
	private Set<String> dependencyIds = new HashSet<String>();
	
	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getVendor() {
		return vendor;
	}

	public final void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public final String getVersion() {
		return version;
	}

	public final void setVersion(String version) {
		this.version = version;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	@Override
	public final Set<String> getDependencyIds() {
		return Collections.unmodifiableSet(dependencyIds);
	}

	public final void setDependencyIds(Set<String> dependencyIds) {
		this.dependencyIds = new HashSet<String>(dependencyIds);
	}

	@Override
	public void postStart() {
	}

	@Override
	public void preStop() {
	}
	
}
