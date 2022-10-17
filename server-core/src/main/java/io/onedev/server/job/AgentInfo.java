package io.onedev.server.job;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.AgentData;

public class AgentInfo {

	private final Long id;
	
	private final AgentData data;
	
	private final Session session;
	
	public AgentInfo(Long id, AgentData data, Session session) {
		this.id = id;
		this.data = data;
		this.session = session;
	}

	public Long getId() {
		return id;
	}

	public AgentData getData() {
		return data;
	}

	public Session getSession() {
		return session;
	}
	
}
