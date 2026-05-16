package io.onedev.server.service;

import java.util.concurrent.Future;

import org.jspecify.annotations.Nullable;

import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.support.AgentCallable;

public interface ResourceService {

	<T> Future<T> submitServerTask(@Nullable String pinnedServerAddress, String resourceName, 
			int totalConcurrency, int requiredConcurrency, ClusterTask<T> task);

	<T> Future<T> submitAgentTask(@Nullable Long pinnedAgentId, AgentQuery agentQuery, String resourceName,
			int totalConcurrency, int requiredConcurrency, AgentCallable<T> task);
	
}
