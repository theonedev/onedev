package com.pmease.commons.wicket;

import org.apache.wicket.request.resource.CssPackageResource;
import org.apache.wicket.request.resource.CssResourceReference;

@SuppressWarnings("serial")
public class VersionlessCssResourceReference extends CssResourceReference {

	public VersionlessCssResourceReference(Class<?> scope, String name) {
		super(scope, name);
	}

	@Override
	public CssPackageResource getResource() {
		CssPackageResource resource = super.getResource();
		resource.setCachingEnabled(false);
		return resource;
	}

}
