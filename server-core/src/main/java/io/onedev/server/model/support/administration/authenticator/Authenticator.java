package io.onedev.server.model.support.administration.authenticator;

import java.io.Serializable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import io.onedev.server.util.usage.Usage;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.GroupChoice;

@Editable
@JsonTypeInfo(use=Id.CLASS)
public abstract class Authenticator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String defaultGroup;
	
	private boolean createUserAsGuest;
	
	private int timeout = 300;

	@Editable(order=10000, description = "Whether or not to create authenticated user as <a href='https://docs.onedev.io/concepts#guest-user' target='_blank'>guest</a>")
	public boolean isCreateUserAsGuest() {
		return createUserAsGuest;
	}

	public void setCreateUserAsGuest(boolean createUserAsGuest) {
		this.createUserAsGuest = createUserAsGuest;
	}
	
	@Editable(order=15000, placeholder="No default group", description="Optionally add newly authenticated user to "
			+ "specified group if membership information is not retrieved")
	@GroupChoice
	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	@Editable(order=20000, description="Specify network timeout in seconds when authenticate through this system")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		if (oldName.equals(defaultGroup))
			defaultGroup = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (groupName.equals(defaultGroup))
			usage.add("default group");
		return usage.prefix("external authenticator");
	}
	
	public abstract Authenticated authenticate(UsernamePasswordToken token) throws AuthenticationException;
	
	public abstract boolean isManagingMemberships();
	
}
