package com.turbodev.server.web.page.admin.authenticator;

import java.io.Serializable;

import com.turbodev.server.security.authenticator.Authenticator;
import com.turbodev.server.util.editable.annotation.Editable;
import com.turbodev.server.util.editable.annotation.NullChoice;

@Editable
public class AuthenticatorHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private Authenticator authenticator;

	@Editable
	@NullChoice("No external authentication")
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

}
