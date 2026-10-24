package io.onedev.server.web.component.user.sshkey;

import java.io.Serial;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class SshKeyCssResourceReference extends BaseDependentCssResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public SshKeyCssResourceReference() {
		super(SshKeyCssResourceReference.class, "ssh-key.css");
	}

}
