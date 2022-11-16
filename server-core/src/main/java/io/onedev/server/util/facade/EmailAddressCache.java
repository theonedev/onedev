package io.onedev.server.util.facade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;

import io.onedev.server.model.User;
import io.onedev.server.util.MapProxy;

public class EmailAddressCache extends MapProxy<Long, EmailAddressFacade> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public EmailAddressCache(Map<Long, EmailAddressFacade> delegate) {
		super(delegate);
	}
	
	@Override
	public EmailAddressCache clone() {
		return new EmailAddressCache(new HashMap<>(delegate));
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
	public EmailAddressFacade findPrimary(Long userId) {
		for (EmailAddressFacade facade: values()) {
			if (facade.isPrimary() && facade.getOwnerId().equals(userId)) 
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
