package io.onedev.server.entitymanager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.AgentData;
import io.onedev.server.model.Agent;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;

public interface AgentManager extends EntityManager<Agent> {
	
	String getAgentVersion();
	
	Collection<String> getAgentLibs();
	
	Long agentConnected(AgentData data, Session session);
	
	void agentDisconnected(Long agentId);
	
	Agent findByName(String name);

	Map<Long, UUID> getAgentServers();

	Collection<String> getOsNames();
	
	Collection<String> getOsArchs();
	
	List<Agent> query(EntityQuery<Agent> agentQuery, int firstResult, int maxResults);
	
	int count(@Nullable Criteria<Agent> agentCriteria);
	
	void restart(Agent agent);
	
	void pause(Agent agent);
	
	void resume(Agent agent);
	
	void attributesUpdated(Agent agent);
	
	List<String> getAgentLog(Agent agent);
	
	@Nullable
	Session getAgentSession(Long agentId);
	
}