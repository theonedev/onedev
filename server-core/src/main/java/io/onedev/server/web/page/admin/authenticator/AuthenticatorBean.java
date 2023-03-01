package io.onedev.server.web.page.admin.authenticator;

import java.io.Serializable;

import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.annotation.Editable;

@Editable
public class AuthenticatorBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Authenticator authenticator;

	@Editable(placeholder="No external authentication")
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

}
