package io.onedev.server.model.support.administration;

import java.io.Serializable;

import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enableAnonymousAccess = false;
	
	private boolean enableSelfRegister = true;
	
	private String defaultLoginGroup;
	
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

	@Editable(order=300, name="Default Group for Sign-In Users", description="Optionally specify a default group "
			+ "for all users signed in")
	@GroupChoice
	public String getDefaultLoginGroup() {
		return defaultLoginGroup;
	}

	public void setDefaultLoginGroup(String defaultLoginGroup) {
		this.defaultLoginGroup = defaultLoginGroup;
	}

	public void onRenameGroup(String oldName, String newName) {
		if (oldName.equals(defaultLoginGroup))
			defaultLoginGroup = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (groupName.equals(defaultLoginGroup))
			usage.add("default group for sign-in users");
		return usage.prefix("security setting");
	}
	
}
