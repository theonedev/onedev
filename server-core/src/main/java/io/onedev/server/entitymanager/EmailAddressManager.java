package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.model.EmailAddress;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;
import io.onedev.server.util.facade.EmailAddressCache;
import io.onedev.server.util.facade.EmailAddressFacade;

public interface EmailAddressManager extends EntityManager<EmailAddress> {

	@Nullable
	EmailAddress findByValue(String value);
	
	@Nullable
	EmailAddressFacade findFacadeByValue(String value);
	
	@Nullable 
	EmailAddressFacade findPrimaryFacade(Long userId);
	
	@Nullable
	EmailAddress findPrimary(User user);

	@Nullable
	EmailAddress findGit(User user);
	
	@Nullable
	EmailAddress findByPersonIdent(PersonIdent personIdent);
	
	void setAsPrimary(EmailAddress emailAddress);
	
	void useForGitOperations(EmailAddress emailAddress);
	
	void sendVerificationEmail(EmailAddress emailAddress);

	EmailAddressCache cloneCache();
}