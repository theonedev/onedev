package com.gitplex.server.web.page.depot.blob;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.eclipse.jgit.lib.ObjectId;

import com.gitplex.server.web.util.AjaxEvent;

public class RevisionResolveEvent extends AjaxEvent {

	private final ObjectId resolvedRevision;
	
	public RevisionResolveEvent(IPartialPageRequestHandler partialPageRequestHandler, ObjectId resolvedRevision) {
		super(partialPageRequestHandler);
		this.resolvedRevision = resolvedRevision;
	}

	public ObjectId getResolvedRevision() {
		return resolvedRevision;
	}

}
