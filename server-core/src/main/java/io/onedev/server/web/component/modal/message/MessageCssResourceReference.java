package io.onedev.server.web.component.modal.message;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class MessageCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public MessageCssResourceReference() {
		super(MessageCssResourceReference.class, "message.css");
	}

}
