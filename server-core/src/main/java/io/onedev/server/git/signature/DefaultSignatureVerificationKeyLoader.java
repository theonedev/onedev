package io.onedev.server.git.signature;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GpgSetting;

@Singleton
public class DefaultSignatureVerificationKeyLoader implements SignatureVerificationKeyLoader {

	private final SettingManager settingManager;
	
	private final GpgKeyManager gpgKeyManager;
	
	@Inject
	public DefaultSignatureVerificationKeyLoader(SettingManager settingManager, GpgKeyManager gpgKeyManager) {
		this.settingManager = settingManager;
		this.gpgKeyManager = gpgKeyManager;
	}
	
	@Override
	public SignatureVerificationKey getSignatureVerificationKey(long keyId) {
		GpgSetting gpgSetting = settingManager.getGpgSetting();
		SignatureVerificationKey verificationKey = gpgSetting.findSignatureVerificationKey(keyId);
		if (verificationKey == null) 
			verificationKey = gpgKeyManager.findSignatureVerificationKey(keyId);
		return verificationKey;
	}

}
