package io.onedev.server;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.location.GitLocation;

@Singleton
public class GitLocationProvider implements Provider<GitLocation> {

	private final SettingManager settingManager;
	
	@Inject
	public GitLocationProvider(SettingManager settingManager) {
		this.settingManager = settingManager;
	}
	
	@Override
	public GitLocation get() {
		return settingManager.getSystemSetting().getGitLocation();
	}

}
