package io.onedev.server.web.resource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class AgentLogResourceReference extends ResourceReference {

	private static final long serialVersionUID = 1L;

	public AgentLogResourceReference() {
		super("agent-log");
	}

	@Override
	public IResource getResource() {
		return new AgentLogResource();
	}

}
