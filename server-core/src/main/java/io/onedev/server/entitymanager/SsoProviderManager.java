package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.SsoProvider;
import io.onedev.server.persistence.dao.EntityManager;

public interface SsoProviderManager extends EntityManager<SsoProvider> {
	
	void createOrUpdate(SsoProvider ssoProvider);
		
	@Nullable
	SsoProvider find(String name);

}
