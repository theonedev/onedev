package io.onedev.server.util.facade;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;
import io.onedev.server.util.MapProxy;

public class EmailAddressCache extends MapProxy<Long, EmailAddressFacade> {

	private static final long serialVersionUID = 1L;

	// Keep only lookup hints: email addresses can change on another cluster node.
	private final Map<String, Long> idsByValue = new ConcurrentHashMap<>();

	// Validate each hint against the replicated record, including primary-address changes.
	private final Map<Long, Long> primaryIdsByUser = new ConcurrentHashMap<>();
	
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

		var id = idsByValue.get(value);
		if (id != null) {
			var facade = get(id);
			if (facade != null && facade.getValue().equals(value))
				return facade;
			idsByValue.remove(value, id);
		}
		for (EmailAddressFacade facade: values()) {
			if (facade.getValue().equals(value)) {
				idsByValue.put(value, facade.getId());
				return facade;
			}
		}
		return null;
	}
	
	@Nullable
	public EmailAddressFacade findPrimary(Long userId) {
		var id = primaryIdsByUser.get(userId);
		if (id != null) {
			var facade = get(id);
			if (facade != null && facade.isPrimary() && facade.getOwnerId().equals(userId))
				return facade;
			primaryIdsByUser.remove(userId, id);
		}
		for (EmailAddressFacade facade: values()) {
			if (facade.isPrimary() && facade.getOwnerId().equals(userId)) {
				primaryIdsByUser.put(userId, facade.getId());
				return facade;
			}
		}
		return null;
	}
	
}
