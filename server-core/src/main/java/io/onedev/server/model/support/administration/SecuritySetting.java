package io.onedev.server.model.support.administration;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SecuritySetting.class);

	private boolean enableAnonymousAccess = false;
	
	private boolean enableSelfRegister = true;
	
	private String defaultLoginGroupName;
	
	private boolean enforce2FA;
	
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

	@Editable(order=300, name="Default Login Group", description="Optionally specify a default group "
			+ "for all users logged in")
	@GroupChoice
	public String getDefaultLoginGroupName() {
		return defaultLoginGroupName;
	}

	public void setDefaultLoginGroupName(String defaultLoginGroupName) {
		this.defaultLoginGroupName = defaultLoginGroupName;
	}

	@Editable(order=400, name="Enforce Two-factor Authentication", description="Check this to enforce "
			+ "all users to set up two-factor authentication upon next login. Users will not be able "
			+ "to disable two-factor authentication themselves if this option is set")
	public boolean isEnforce2FA() {
		return enforce2FA;
	}

	public void setEnforce2FA(boolean enforce2FA) {
		this.enforce2FA = enforce2FA;
	}

	@Nullable
	public Group getDefaultLoginGroup() {
		if (defaultLoginGroupName != null) {
       		Group group = OneDev.getInstance(GroupManager.class).find(defaultLoginGroupName);
       		if (group == null) 
       			logger.error("Unable to find default login group: " + defaultLoginGroupName);
       		else
       			return group;
		}
		return null;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		if (oldName.equals(defaultLoginGroupName))
			defaultLoginGroupName = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (groupName.equals(defaultLoginGroupName))
			usage.add("default group for sign-in users");
		return usage.prefix("security setting");
	}
	
}
