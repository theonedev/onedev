package io.onedev.server.git;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import io.onedev.server.service.SettingService;
import io.onedev.server.git.location.GitLocation;

@Singleton
public class GitLocationProvider implements Provider<GitLocation> {

	private final SettingService settingService;
	
	@Inject
	public GitLocationProvider(SettingService settingService) {
		this.settingService = settingService;
	}
	
	@Override
	public GitLocation get() {
		return settingService.getSystemSetting().getGitLocation();
	}

}
