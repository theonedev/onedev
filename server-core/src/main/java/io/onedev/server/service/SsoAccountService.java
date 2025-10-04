package io.onedev.server.service;

import javax.annotation.Nullable;

import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.SsoProvider;

public interface SsoAccountService extends EntityService<SsoAccount> {
	
	void create(SsoAccount ssoAccount);
		
	@Nullable
	SsoAccount find(SsoProvider provider, String subject);

}
