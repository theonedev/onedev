package com.gitplex.server.web.page.depot.blob.navigator;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import com.gitplex.server.web.util.AjaxEvent;

public class BlobNameChanging extends AjaxEvent {

	public BlobNameChanging(IPartialPageRequestHandler partialPageRequestHandler) {
		super(partialPageRequestHandler);
	}

}
