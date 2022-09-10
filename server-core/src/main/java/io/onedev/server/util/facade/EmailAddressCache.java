package io.onedev.server.util.facade;

import java.util.HashMap;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.model.User;

public class EmailAddressCache extends HashMap<Long, EmailAddressFacade> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public EmailAddressCache clone() {
		EmailAddressCache clone = new EmailAddressCache();
		clone.putAll(this);
		return clone;
	}
	
	@Nullable
	public EmailAddressFacade findByValue(String value) {
		value = value.toLowerCase();
		for (EmailAddressFacade facade: values()) {
			if (facade.getValue().equals(value))
				return facade;
		}
		return null;
	}

	@Nullable
    public EmailAddressFacade findByPersonIdent(PersonIdent personIdent) {
    	if (StringUtils.isNotBlank(personIdent.getEmailAddress()))
    		return findByValue(personIdent.getEmailAddress());
    	else
    		return null;
    }
	
	@Nullable
	public EmailAddressFacade findPrimary(User user) {
		for (EmailAddressFacade facade: values()) {
			if (facade.isPrimary() && facade.getOwnerId().equals(user.getId())) 
				return facade;
		}
		return null;
	}
	
	@Nullable
	public EmailAddressFacade findGit(User user) {
		for (EmailAddressFacade facade: values()) {
			if (facade.isGit() && facade.getOwnerId().equals(user.getId())) 
				return facade;
		}
		return null;
	}
	
}
