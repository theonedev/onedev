package io.onedev.server;

import javax.inject.Singleton;

@Singleton
public class DefaultFeatureManager implements FeatureManager {
	
	@Override
	public boolean isEEAvailable() {
		return false;
	}

	@Override
	public boolean isEELicensed() {
		return false;
	}
	
}
