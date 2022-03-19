package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.model.EmailAddress;
import io.onedev.server.persistence.dao.EntityManager;

public interface EmailAddressManager extends EntityManager<EmailAddress> {

	@Nullable
	EmailAddress findByValue(String value);
	
	@Nullable
	EmailAddress findByPersonIdent(PersonIdent personIdent);
	
	void setAsPrimary(EmailAddress emailAddress);
	
	void useForGitOperations(EmailAddress emailAddress);
	
	void sendVerificationEmail(EmailAddress emailAddress);
	
}