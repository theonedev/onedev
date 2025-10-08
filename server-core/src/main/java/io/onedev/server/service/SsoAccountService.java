package io.onedev.server.service;

import org.jspecify.annotations.Nullable;

import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.SsoProvider;

public interface SsoAccountService extends EntityService<SsoAccount> {
	
	void create(SsoAccount ssoAccount);
		
	@Nullable
	SsoAccount find(SsoProvider provider, String subject);

}
