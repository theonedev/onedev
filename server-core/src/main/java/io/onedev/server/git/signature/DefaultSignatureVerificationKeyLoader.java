package io.onedev.server.git.signature;

import javax.inject.Singleton;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GpgKeyManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.GpgKey;
import io.onedev.server.model.support.administration.GpgSetting;

@Singleton
public class DefaultSignatureVerificationKeyLoader implements SignatureVerificationKeyLoader {

	@Override
	public SignatureVerificationKey getSignatureVerificationKey(long keyId) {
		GpgSetting gpgSetting = OneDev.getInstance(SettingManager.class).getGpgSetting();
		SignatureVerificationKey verificationKey = gpgSetting.findSignatureVerificationKey(keyId);
		if (verificationKey == null) {
			GpgKey gpgKey = OneDev.getInstance(GpgKeyManager.class).findByKeyId(keyId);
			if (gpgKey != null)
				verificationKey = gpgKey.getSignatureVerificationKey();
		}
		return verificationKey;
	}

}
