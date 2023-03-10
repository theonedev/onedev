package io.onedev.server.entitymanager;

import java.util.List;

import javax.annotation.Nullable;

import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityManager;

public interface AgentTokenManager extends EntityManager<AgentToken> {
	
	void create(AgentToken token);
	
	void update(AgentToken token);
	
	@Nullable
	AgentToken find(String value);
	
	List<AgentToken> queryUnused();
	
	void deleteUnused();
	
}
