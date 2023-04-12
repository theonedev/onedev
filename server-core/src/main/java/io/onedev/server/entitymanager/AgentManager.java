package io.onedev.server.entitymanager;

import io.onedev.agent.AgentData;
import io.onedev.server.model.Agent;
import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.util.criteria.Criteria;
import org.eclipse.jetty.websocket.api.Session;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface AgentManager extends EntityManager<Agent> {
	
	String getAgentVersion();
	
	Collection<String> getAgentLibs();
	
	Long agentConnected(AgentData data, Session session);
	
	void agentDisconnected(Long agentId);
	
	void disconnect(Long agentId);
	
	@Nullable
	Agent findByName(String name);
	
	@Nullable
	Agent findByToken(AgentToken token);

	Collection<Long> getOnlineAgents();
	
	@Nullable
	String getAgentServer(Long agentId);

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