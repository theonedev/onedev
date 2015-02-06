package com.pmease.commons.wicket;

import org.apache.wicket.request.resource.JavaScriptPackageResource;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

@SuppressWarnings("serial")
public class VersionlessJavaScriptResourceReference extends JavaScriptResourceReference {

	public VersionlessJavaScriptResourceReference(Class<?> scope, String name) {
		super(scope, name);
	}

	@Override
	public JavaScriptPackageResource getResource() {
		JavaScriptPackageResource resource = super.getResource();
		resource.setCachingEnabled(false);
		return resource;
	}

}
