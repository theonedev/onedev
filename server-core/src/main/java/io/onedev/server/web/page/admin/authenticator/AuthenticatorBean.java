package io.onedev.server.web.page.admin.authenticator;

import java.io.Serializable;

import io.onedev.server.model.support.administration.authenticator.Authenticator;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable
public class AuthenticatorBean implements Serializable {

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
