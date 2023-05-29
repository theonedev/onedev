package io.onedev.server.util.facade;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.MapProxy;
import io.onedev.server.util.Similarities;

public class UserCache extends MapProxy<Long, UserFacade> implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public UserCache(Map<Long, UserFacade> delegate) {
		super(delegate);
	}
	
	@Nullable
	public UserFacade findByName(String name) {
		name = name.toLowerCase();
		for (UserFacade facade: values()) {
			if (name.equals(facade.getName()))
				return facade;
		}
		return null;
	}
	
	@Nullable
	public UserFacade findByFullName(String fullName) {
		for (UserFacade facade: values()) {
			if (fullName.equals(facade.getFullName()))
				return facade;
		}
		return null;
	}
	
	@Nullable
	public UserFacade findByAccessToken(String accessTokenValue) {
		for (UserFacade facade: values()) {
			for (var accessToken: facade.getAccessTokens()) {
				if (accessTokenValue.equals(accessToken.getValue()) && !accessToken.isExpired())
					return facade;
			}
		}
		return null;
	}
	
	public double getSimilarScore(User user, @Nullable String term) {
		UserFacade facade = get(user.getId());
		double scoreOfName = Similarities.getSimilarScore(facade.getName(), term);
		double scoreOfFullName = Similarities.getSimilarScore(facade.getFullName(), term);
		return Math.max(scoreOfName, scoreOfFullName);
	}
	
	@Override
	public UserCache clone() {
		return new UserCache(new HashMap<>(delegate));
	}
	
	public Collection<User> getUsers() {
		UserManager userManager = OneDev.getInstance(UserManager.class);
		return keySet().stream().filter(it->it>0).map(it->userManager.load(it)).collect(Collectors.toSet());
	}
	
	public Comparator<User> comparingDisplayName(Collection<User> topUsers) {
		return (o1, o2) -> {
			if (topUsers.contains(o1)) {
				if (topUsers.contains(o2))
					return get(o1.getId()).getDisplayName().compareTo(get(o2.getId()).getDisplayName());
				else
					return -1;
			} else if (topUsers.contains(o2)) {
				return 1;
			} else {
				return get(o1.getId()).getDisplayName().compareTo(get(o2.getId()).getDisplayName());
			}
		};		
	}
	
}
