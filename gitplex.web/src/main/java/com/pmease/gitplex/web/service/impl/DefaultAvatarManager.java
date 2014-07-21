package com.pmease.gitplex.web.service.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.web.service.AvatarManager;
import com.pmease.gitplex.web.util.Gravatar;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static String BASE_AVATAR_URL = "/site/avatars/";
	
	private static String UNKNOWN_AVATAR = "unknown.jpg";

	private static String DEFAULT_AVATAR = "default.jpg";
	
	private final ConfigManager configManager;
	
	private final UserManager userManager;
	
	@Inject
	public DefaultAvatarManager(ConfigManager configManager, UserManager userManager) {
		this.configManager = configManager;
		this.userManager = userManager;
	}
	
	@Override
	public String getAvatarUrl(User user) {
		if (user == null) {
			return BASE_AVATAR_URL + UNKNOWN_AVATAR;
		} else if (user.getAvatarUpdateDate() != null) { 
			String url = BASE_AVATAR_URL + user.getId();
			url += "?version=" + user.getAvatarUpdateDate().getTime();
			return url;
		} else if (configManager.getSystemSetting().isGravatarEnabled()) {
			return Gravatar.getURL(user.getEmail(), GRAVATAR_SIZE);
		} else {
			return getDefaultAvatarUrl();
		}
	}

	@Override
	public String getAvatarUrl(String email) {
		User user = userManager.findByEmail(email);
		if (user != null)
			return getAvatarUrl(user);
		else if (configManager.getSystemSetting().isGravatarEnabled())
			return Gravatar.getURL(email);
		else
			return getDefaultAvatarUrl();
	}

	@Override
	public String getDefaultAvatarUrl() {
		return BASE_AVATAR_URL + DEFAULT_AVATAR;
	}

}
