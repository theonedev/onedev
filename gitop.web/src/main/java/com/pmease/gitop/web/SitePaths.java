package com.pmease.gitop.web;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.model.User;

@Singleton
public class SitePaths {
	private final ConfigManager configManager;
	
	public static SitePaths get() {
		return AppLoader.getInstance(SitePaths.class);
	}
	
	@Inject
	SitePaths(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	public File installDir() {
		return Bootstrap.installDir;
	}
	
	public File dataDir() {
		return new File(configManager.getStorageSetting().getDataPath());
	}
	
	public File avatarsDir() {
		return new File(dataDir(), "avatars");
	}
	
	public File userAvatarDir(Long id) {
		return new File(avatarsDir(), "users/" + checkNotNull(id));
	}
	
	public File userAvatarDir(User user) {
		return userAvatarDir(checkNotNull(user).getId());
	}
	
	public File tempDir() {
		return new File(installDir(), "temp");
	}
	
	public File uploadsDir() {
		return new File(dataDir(), "uploads");
	}
}
