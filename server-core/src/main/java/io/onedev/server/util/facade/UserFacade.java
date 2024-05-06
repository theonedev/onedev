package io.onedev.server.util.facade;

import io.onedev.server.model.User;

import javax.annotation.Nullable;

public class UserFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;
	
	private final boolean guest;
	
	public UserFacade(Long id, String name, @Nullable String fullName, boolean guest) {
		super(id);
		this.name = name;
		this.fullName = fullName;
		this.guest = guest;
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public boolean isGuest() {
		return guest;
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

	public boolean isEffectiveGuest() {
		return guest && !isRoot() && !isSystem();
	}
	
	public static UserFacade of(@Nullable User user) {
		if (user != null)
			return user.getFacade();
		else 
			return null;
	}
	
}
