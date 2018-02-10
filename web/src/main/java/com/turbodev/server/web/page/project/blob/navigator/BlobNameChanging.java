package com.turbodev.server.web.page.project.blob.navigator;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.turbodev.server.web.util.AjaxPayload;

public class BlobNameChanging extends AjaxPayload {

	public BlobNameChanging(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
