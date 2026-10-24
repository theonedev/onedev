package io.onedev.server.web.page.admin.buildsetting.agent;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class AgentCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public AgentCssResourceReference() {
		super(AgentCssResourceReference.class, "agent.css");
	}

}
