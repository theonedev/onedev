package com.pmease.commons.loader;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractPlugin implements Plugin {
	
	private String id;
	
	private String name;
	
	private String vendor;
	
	private String version;
	
	private String description;
	
	private Date date;
	
	private boolean product;
	
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isProduct() {
		return product;
	}

	public void setProduct(boolean product) {
		this.product = product;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	@Override
	public final Set<String> getDependencies() {
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
