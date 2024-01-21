package io.onedev.server.util.facade;

import io.onedev.server.model.User;
import io.onedev.server.model.support.AccessToken;

import javax.annotation.Nullable;
import java.util.List;

public class UserFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;
	
	private final boolean guest;
	
	private final List<AccessToken> accessTokens;
	
	public UserFacade(Long id, String name, @Nullable String fullName, boolean guest, List<AccessToken> accessTokens) {
		super(id);
		this.name = name;
		this.fullName = fullName;
		this.guest = guest;
		this.accessTokens = accessTokens;
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

	public List<AccessToken> getAccessTokens() {
		return accessTokens;
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
