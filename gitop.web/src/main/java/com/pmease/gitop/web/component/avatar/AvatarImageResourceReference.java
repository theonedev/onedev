package com.pmease.gitop.web.component.avatar;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.SharedResourceReference;

public class AvatarImageResourceReference extends SharedResourceReference {

	private static final long serialVersionUID = 1L;

	public static String AVATAR_RESOURCE = "gitop-avatar-resource";

	public AvatarImageResourceReference() {
		super(AVATAR_RESOURCE);
	}

	@Override
	public IResource getResource() {
		return new AvatarImageResource();
	}

}
