package io.onedev.server.git.signatureverification.gpg;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.signatureverification.VerificationSuccessful;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.web.component.gitsignature.GpgVerificationDetailPanel;
import org.apache.wicket.Component;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import static io.onedev.server.util.GpgUtils.getEmailAddresses;

public class GpgVerificationSuccessful implements VerificationSuccessful {
	
	private final GpgSigningKey signingKey;
	
	public GpgVerificationSuccessful(GpgSigningKey signingKey) {
		this.signingKey = signingKey;
	}
	
	@Override
	public Component renderDetail(String componentId, RevObject revObject) {
		var publicKey = signingKey.getPublicKey();
		String message;
		if (signingKey.getEmailAddresses() != null) {
			if (revObject instanceof RevCommit)
				message = "Signature verified successfully with committer's GPG key";
			else
				message = "Signature verified successfully with tagger's GPG key";
		} else if (getGpgSetting().getTrustedSignatureVerificationKey(publicKey.getKeyID()) != null) {
			message = "Signature verified successfully with trusted GPG key";
		} else {
			message = "Signature verified successfully with OneDev GPG key";
		}
		
		return new GpgVerificationDetailPanel(componentId, true, message, 
				publicKey.getKeyID(), getEmailAddresses(publicKey));
	}
	
	private GpgSetting getGpgSetting() {
		return OneDev.getInstance(SettingManager.class).getGpgSetting();
	}
	
}
