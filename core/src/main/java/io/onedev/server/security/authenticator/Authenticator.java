package io.onedev.server.security.authenticator;

import java.io.Serializable;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;

@Editable
public abstract class Authenticator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<String> defaultGroupNames;
	
	private int timeout = 300;

	@Editable(order=10000, description="Specify network timeout in seconds when authenticate through this system")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Editable(order=20000, description="Optionally add newly authenticated user to specified groups if group "
			+ "information is not available in external authentication system")
	@GroupChoice
	public List<String> getDefaultGroupNames() {
		return defaultGroupNames;
	}

	public void setDefaultGroupNames(List<String> defaultGroupNames) {
		this.defaultGroupNames = defaultGroupNames;
	}

	public void onRenameGroup(String oldName, String newName) {
		int index = defaultGroupNames.indexOf(oldName);
		if (index != -1)
			defaultGroupNames.set(index, newName);
	}
	
	public void onDeleteGroup(String groupName) {
		defaultGroupNames.remove(groupName);
	}
	
	public abstract Authenticated authenticate(UsernamePasswordToken token) throws AuthenticationException;
	
}
