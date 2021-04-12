package io.onedev.server.model.support.administration;

import java.io.Serializable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enableAnonymousAccess = false;
	
	private boolean enableSelfRegister = true;
	
	@Editable(order=100, description="Whether or not to allow anonymous users to access this server")
	public boolean isEnableAnonymousAccess() {
		return enableAnonymousAccess;
	}

	public void setEnableAnonymousAccess(boolean enableAnonymousAccess) {
		this.enableAnonymousAccess = enableAnonymousAccess;
	}

	@Editable(order=200, name="Enable Self Sign-Up", description="User can sign up if this option is enabled")
	public boolean isEnableSelfRegister() {
		return enableSelfRegister;
	}

	public void setEnableSelfRegister(boolean enableSelfRegister) {
		this.enableSelfRegister = enableSelfRegister;
	}

}
