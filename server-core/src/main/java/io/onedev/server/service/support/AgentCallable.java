package io.onedev.server.service.support;

import java.io.Serializable;

public interface AgentCallable<T> extends Serializable {
	
	T call(Long agentId);
	
}
