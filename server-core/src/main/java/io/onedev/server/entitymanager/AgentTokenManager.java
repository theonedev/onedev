package io.onedev.server.entitymanager;

import io.onedev.server.model.AgentToken;
import io.onedev.server.persistence.dao.EntityManager;

import javax.annotation.Nullable;
import java.util.List;

public interface AgentTokenManager extends EntityManager<AgentToken> {
	
	void createOrUpdate(AgentToken token);
	
	@Nullable
	AgentToken find(String value);
	
	List<AgentToken> queryUnused();
	
	void deleteUnused();
	
}
