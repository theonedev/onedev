package io.onedev.server.util.facade;

import io.onedev.server.model.User;

import org.jspecify.annotations.Nullable;

public class UserFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;

	private final boolean serviceAccount;

	private final boolean disabled;
	
	public UserFacade(Long id, String name, @Nullable String fullName, boolean serviceAccount, boolean disabled) {
		super(id);
		this.name = name;
		this.fullName = fullName;
		this.serviceAccount = serviceAccount;
		this.disabled = disabled;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}

	public boolean isRoot() {
		return User.ROOT_ID.equals(getId());
	}

	public boolean isSystem() {
		return User.SYSTEM_ID.equals(getId());
	}

	public boolean isUnknown() {
		return User.UNKNOWN_ID.equals(getId());
	}
	
	public static UserFacade of(@Nullable User user) {
		if (user != null)
			return user.getFacade();
		else 
			return null;
	}

	public boolean isServiceAccount() {
		return serviceAccount;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
}
