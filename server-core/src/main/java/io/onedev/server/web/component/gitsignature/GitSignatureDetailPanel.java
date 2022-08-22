package io.onedev.server.web.component.gitsignature;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.git.signature.SignatureUnverified;
import io.onedev.server.git.signature.SignatureVerification;
import io.onedev.server.git.signature.SignatureVerificationKey;
import io.onedev.server.git.signature.SignatureVerified;
import io.onedev.server.model.support.administration.GpgSetting;
import io.onedev.server.util.GpgUtils;
import io.onedev.server.web.component.MultilineLabel;

@SuppressWarnings("serial")
abstract class GitSignatureDetailPanel extends Panel {

	public GitSignatureDetailPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		SignatureVerificationKey key = getVerification().getVerificationKey(); 
		if (getVerification() instanceof SignatureVerified) {
			String message;
			if (getVerification().getVerificationKey().shouldVerifyDataWriter()) {
				if (getRevObject() instanceof RevCommit)
					message = "Signature verified successfully with committer's GPG key";
				else
					message = "Signature verified successfully with tagger's GPG key";
			} else if (getGpgSetting().getTrustedSignatureVerificationKey(key.getPublicKey().getKeyID()) != null) {
				message = "Signature verified successfully with trusted GPG key"; 
			} else {
				message = "Signature verified successfully with OneDev GPG key";
			}
			add(new Label("message", message).add(AttributeAppender.append("class", "alert alert-light-success mb-0")));
		} else {
			SignatureUnverified unverified = (SignatureUnverified) getVerification();
			if (key != null) {
				add(new Label("message", unverified.getErrorMessage())
						.add(AttributeAppender.append("class", "alert alert-light-danger mb-0")));
			} else {
				add(new Label("message", unverified.getErrorMessage())
						.add(AttributeAppender.append("class", "text-danger")));
			}
		}

		if (key != null) {
			add(new Label("keyId", GpgUtils.getKeyIDString(key.getPublicKey().getKeyID())));
			add(new MultilineLabel("emailAddresses", StringUtils.join(key.getEmailAddresses(), "\n")));
		} else {
			add(new WebMarkupContainer("keyId").setVisible(false));
			add(new WebMarkupContainer("emailAddress").setVisible(false));
		}
	}
	
	private GpgSetting getGpgSetting() {
		return OneDev.getInstance(SettingManager.class).getGpgSetting();
	}

	protected abstract SignatureVerification getVerification();
	
	protected abstract RevObject getRevObject();
	
}
