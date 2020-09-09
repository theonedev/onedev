package io.onedev.server.model.support.administration;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;
import io.onedev.server.web.editable.annotation.ShowCondition;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean enableAnonymousAccess = false;
	
	private String anonymousGroup;

	private boolean enableSelfRegister = true;
	
	@Editable(order=100, description="Whether or not to allow anonymous users to access this server")
	public boolean isEnableAnonymousAccess() {
		return enableAnonymousAccess;
	}

	public void setEnableAnonymousAccess(boolean enableAnonymousAccess) {
		this.enableAnonymousAccess = enableAnonymousAccess;
	}

	@Editable(order=150, description="Specify the group to grant permissions for anonymous access")
	@GroupChoice
	@ShowCondition("isAnonymousGroupVisible")
	@NotEmpty
	public String getAnonymousGroup() {
		return anonymousGroup;
	}

	public void setAnonymousGroup(String anonymousGroup) {
		this.anonymousGroup = anonymousGroup;
	}
	
	@SuppressWarnings("unused")
	private static boolean isAnonymousGroupVisible() {
		return (boolean) EditContext.get().getInputValue("enableAnonymousAccess");
	}

	@Editable(order=200, name="Enable Sign-Up", description="User can sign up if this option is enabled")
	public boolean isEnableSelfRegister() {
		return enableSelfRegister;
	}

	public void setEnableSelfRegister(boolean enableSelfRegister) {
		this.enableSelfRegister = enableSelfRegister;
	}

	public void onRenameGroup(String oldName, String newName) {
		if (enableAnonymousAccess && oldName.equals(anonymousGroup))
			anonymousGroup = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (enableAnonymousAccess && groupName.equals(anonymousGroup))
			usage.add("anonymous group");
		return usage.prefix("security setting");
	}
	
}
