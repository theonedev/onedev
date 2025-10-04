package io.onedev.server.service;

import javax.annotation.Nullable;

import io.onedev.server.model.SsoProvider;

public interface SsoProviderService extends EntityService<SsoProvider> {
	
	void createOrUpdate(SsoProvider ssoProvider);
		
	@Nullable
	SsoProvider find(String name);

}
