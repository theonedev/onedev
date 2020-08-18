package io.onedev.server.web.resource;

import org.apache.wicket.request.resource.PackageResourceReference;

public class SvgSpriteResourceReference extends PackageResourceReference {

	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_MOUNT_PATH = "icons.svg";
	
	public static final String RESOURCE_NAME = "create-sprite-from-svg-files.svg";

	public SvgSpriteResourceReference(Class<?> scope) {
		super(scope, RESOURCE_NAME);
	}

}
