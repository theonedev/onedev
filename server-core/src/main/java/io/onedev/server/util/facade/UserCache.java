package io.onedev.server.util.facade;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;
import io.onedev.server.util.Similarities;

public class UserCache extends HashMap<Long, UserFacade> {

	private static final long serialVersionUID = 1L;
	
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
	public UserFacade findByAccessToken(String accessToken) {
		for (UserFacade facade: values()) {
			if (accessToken.equals(facade.getAccessToken()))
				return facade;
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
		UserCache clone = new UserCache();
		clone.putAll(this);
		return clone;
	}
	
	public Collection<User> getUsers() {
		UserManager userManager = OneDev.getInstance(UserManager.class);
		return keySet().stream().filter(it->it>0).map(it->userManager.load(it)).collect(Collectors.toSet());
	}
	
	public Comparator<User> comparingDisplayName(Collection<User> topUsers) {
		return new Comparator<User>() {

			@Override
			public int compare(User o1, User o2) {
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
			}
			
		};		
	}
	
}
