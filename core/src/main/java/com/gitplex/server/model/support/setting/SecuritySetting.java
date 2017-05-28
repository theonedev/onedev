package com.gitplex.server.model.support.setting;

import java.io.Serializable;

import com.gitplex.server.util.editable.annotation.Editable;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enableSelfRegister = true;

	@Editable(description="User can self-register users if this option is enabled")
	public boolean isEnableSelfRegister() {
		return enableSelfRegister;
	}

	public void setEnableSelfRegister(boolean enableSelfRegister) {
		this.enableSelfRegister = enableSelfRegister;
	}
	
}
