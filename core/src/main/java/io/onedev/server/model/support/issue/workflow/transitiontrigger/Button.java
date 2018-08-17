package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import java.io.Serializable;

import io.onedev.server.model.support.authorized.Authorized;

public interface Button extends Serializable {

	String getName();

	public Authorized getAuthorized();
	
}
