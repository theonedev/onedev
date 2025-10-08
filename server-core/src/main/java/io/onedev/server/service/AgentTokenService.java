package io.onedev.server.service;

import io.onedev.server.model.AgentToken;

import org.jspecify.annotations.Nullable;
import java.util.List;

public interface AgentTokenService extends EntityService<AgentToken> {
	
	void createOrUpdate(AgentToken token);
	
	@Nullable
	AgentToken find(String value);
	
	List<AgentToken> queryUnused();
	
	void deleteUnused();
	
}
