package io.onedev.server.service.support;

import java.io.Serializable;

public interface AgentRunnable<T> extends Serializable {
	
	T run(Long agentId);
	
}
