package com.pmease.gitop.core;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.pmease.commons.git.GitConfig;
import com.pmease.gitop.core.manager.ConfigManager;

@Singleton
public class GitConfigProvider implements Provider<GitConfig> {

	private final ConfigManager configManager;
	
	@Inject
	public GitConfigProvider(ConfigManager configManager) {
		this.configManager = configManager;
	}
	
	@Override
	public GitConfig get() {
		return configManager.getSystemSetting().getGitConfig();
	}

}
