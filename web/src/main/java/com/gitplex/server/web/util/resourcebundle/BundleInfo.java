package com.gitplex.server.web.util.resourcebundle;

import org.apache.wicket.request.resource.ResourceReference;

public class BundleInfo<T extends ResourceReference> {
	
	private final Class<?> scope;
	
	private final String name;
	
	private final T[] references;
	
	public BundleInfo(Class<?> scope, String name, T[] references) {
		this.scope = scope;
		this.name = name;
		this.references = references;
	}

	public Class<?> getScope() {
		return scope;
	}

	public String getName() {
		return name;
	}

	public T[] getReferences() {
		return references;
	}
	
}
