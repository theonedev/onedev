package io.onedev.server.util.facade;

import javax.annotation.Nullable;

import io.onedev.commons.utils.matchscore.MatchScoreUtils;
import io.onedev.server.model.User;

public class UserFacade extends EntityFacade {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final String fullName;
	
	private final String email;
	
	private final String uuid;
	
	public UserFacade(User user) {
		super(user.getId());

		name = user.getName();
		fullName = user.getFullName();
		email = user.getEmail();
		uuid = user.getUUID();
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}
	
	public String getUUID() {
		return uuid;
	}

	public boolean isRoot() {
		return getId().equals(User.ROOT_ID);
	}

	public String getDisplayName() {
		if (getFullName() != null)
			return getFullName();
		else
			return getName();
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}

	public double getMatchScore(@Nullable String queryTerm) {
		double scoreOfName = MatchScoreUtils.getMatchScore(name, queryTerm);
		double scoreOfFullName = MatchScoreUtils.getMatchScore(fullName, queryTerm);
		return Math.max(scoreOfName, scoreOfFullName);
	}

	public static UserFacade of(@Nullable User user) {
		return user!=null?new UserFacade(user):null;
	}
	
}
