package io.onedev.server.web.page.admin.authenticator;

import java.io.Serializable;

import io.onedev.server.security.authenticator.Authenticator;
import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.NameOfEmptyValue;

@Editable
public class AuthenticatorHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private Authenticator authenticator;

	@Editable
	@NameOfEmptyValue("No external authentication")
	public Authenticator getAuthenticator() {
		return authenticator;
	}

	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

}
