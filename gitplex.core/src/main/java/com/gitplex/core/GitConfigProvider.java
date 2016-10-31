package com.gitplex.core;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.gitplex.core.manager.ConfigManager;
import com.gitplex.commons.git.GitConfig;

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
