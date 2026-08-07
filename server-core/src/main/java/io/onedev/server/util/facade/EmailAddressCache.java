package io.onedev.server.util.facade;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;
import io.onedev.server.util.MapProxy;

public class EmailAddressCache extends MapProxy<Long, EmailAddressFacade> {

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
		var loginName = User.getLoginName(value);
		if (loginName != null) {
			var user = OneDev.getInstance(UserService.class).findFacadeByName(loginName);
			if (user != null)
				return new EmailAddressFacade(null, user.getId(), value, false, null);
			else
				return null;
		}

		for (EmailAddressFacade facade: values()) {
			if (facade.getValue().equals(value))
				return facade;
		}
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
	
}
