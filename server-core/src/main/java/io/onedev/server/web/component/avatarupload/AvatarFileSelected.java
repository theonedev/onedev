package io.onedev.server.web.component.avatarupload;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.util.AjaxPayload;

public class AvatarFileSelected extends AjaxPayload {

	public AvatarFileSelected(AjaxRequestTarget target) {
		super(target);
	}

}