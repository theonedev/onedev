package io.onedev.server.web.behavior.blamemessage;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class BlameMessageCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public BlameMessageCssResourceReference() {
		super(BlameMessageCssResourceReference.class, "blame-message.css");
	}

}
