package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.SsoAccount;
import io.onedev.server.model.SsoProvider;
import io.onedev.server.persistence.dao.EntityManager;

public interface SsoAccountManager extends EntityManager<SsoAccount> {
	
	void create(SsoAccount ssoAccount);
		
	@Nullable
	SsoAccount find(SsoProvider provider, String subject);

}
