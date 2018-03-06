package io.onedev.server.security.authenticator;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.onedev.server.util.editable.annotation.Editable;
import io.onedev.server.util.editable.annotation.GroupChoice;

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

	public boolean onGroupRename(String oldName, String newName) {
		if (defaultGroupNames.contains(oldName)) {
			defaultGroupNames.remove(oldName);
			defaultGroupNames.add(newName);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean onGroupDelete(String groupName) {
		for (Iterator<String> it = defaultGroupNames.iterator(); it.hasNext();) {
			if (it.next().equals(groupName)) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	public abstract Authenticated authenticate(UsernamePasswordToken token) throws AuthenticationException;
	
}
