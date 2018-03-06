package io.onedev.server.web.page.project.blob.navigator;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.util.AjaxPayload;

public class BlobNameChanging extends AjaxPayload {

	public BlobNameChanging(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
