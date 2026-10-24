package io.onedev.server.web.component.user.contributoravatars;

import java.io.Serial;

import org.apache.wicket.request.resource.CssResourceReference;

public class ContributorAvatarsResourceReference extends CssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public ContributorAvatarsResourceReference() {
		super(ContributorAvatarsResourceReference.class, "contributor-avatars.css");
	}

}
