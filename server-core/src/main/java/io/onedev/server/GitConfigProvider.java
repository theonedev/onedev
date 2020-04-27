package io.onedev.server;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.config.GitConfig;

@Singleton
public class GitConfigProvider implements Provider<GitConfig> {

	private final SettingManager settingManager;
	
	@Inject
	public GitConfigProvider(SettingManager settingManager) {
		this.settingManager = settingManager;
	}
	
	@Override
	public GitConfig get() {
		return settingManager.getSystemSetting().getGitConfig();
	}

}
