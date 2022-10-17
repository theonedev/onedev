package io.onedev.server.job;

import java.io.Serializable;

import javax.annotation.Nullable;

public interface ResourceRunnable extends Serializable {

	void run(@Nullable AgentInfo agentInfo);
	
}
