package com.pmease.gitop.web.service.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.User;
import com.pmease.gitop.web.service.AvatarManager;
import com.pmease.gitop.web.util.Gravatar;

@Singleton
public class DefaultAvatarManager implements AvatarManager {

	private static final int GRAVATAR_SIZE = 256;
	
	private static String BASE_AVATAR_URL = "/site/avatars/";
	
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
		checkNotNull(user, "user");
		
		if (user.getAvatarUpdateDate() != null) { 
			String url = BASE_AVATAR_URL + user.getId();
			url += "?version=" + user.getAvatarUpdateDate().getTime();
			return url;
		} else if (configManager.getSystemSetting().isGravatarEnabled()) {
			return Gravatar.getURL(user.getEmail(), GRAVATAR_SIZE);
		} else {
			return BASE_AVATAR_URL + DEFAULT_AVATAR; 
		}
	}

	@Override
	public String getAvatarUrl(String email) {
		checkNotNull(email, "email");
		
		User user = userManager.findByEmail(email);
		if (user != null)
			return getAvatarUrl(user);
		else if (configManager.getSystemSetting().isGravatarEnabled())
			return Gravatar.getURL(email);
		else
			return BASE_AVATAR_URL + DEFAULT_AVATAR;
	}

	@Override
	public String getDefaultAvatarUrl() {
		return BASE_AVATAR_URL + DEFAULT_AVATAR;
	}

}
