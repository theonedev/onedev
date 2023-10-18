package io.onedev.server.model.support.administration.sso;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.GroupChoice;
import io.onedev.server.util.usage.Usage;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.net.URI;

@Editable
public abstract class SsoConnector implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String name;
	
	private boolean createUserAsGuest;
	
	private String defaultGroup;

	@Editable(order=100, description="Name of the provider will be displayed on login button")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public abstract String getButtonImageUrl();

	@Editable(order=10000, description = "Whether or not to create authenticated user as <a href='https://docs.onedev.io/concepts#guest-user' target='_blank'>guest</a>")
	public boolean isCreateUserAsGuest() {
		return createUserAsGuest;
	}

	public void setCreateUserAsGuest(boolean createUserAsGuest) {
		this.createUserAsGuest = createUserAsGuest;
	}

	@Editable(order=20000, placeholder="No default group", description="Optionally add newly authenticated "
			+ "user to specified group if membership information is not retrieved")
	@GroupChoice
	public String getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(String defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public void onRenameGroup(String oldName, String newName) {
		if (oldName.equals(defaultGroup))
			defaultGroup = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (groupName.equals(defaultGroup))
			usage.add("default group");
		return usage;
	}
	
	public abstract URI getCallbackUri();
	
	public abstract SsoAuthenticated processLoginResponse();
	
	public abstract void initiateLogin();

	public abstract boolean isManagingMemberships();

}
