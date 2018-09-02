package io.onedev.server.security.authenticator;

import java.io.Serializable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class Authenticator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int timeout = 300;
	
	private boolean canCreateProjects;
	
	@Editable(order=10000, description="Specify network timeout in seconds when authenticate through this system")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Editable(order=20000, description="Check this if authenticated users are allowed to create projects")
	public boolean isCanCreateProjects() {
		return canCreateProjects;
	}

	public void setCanCreateProjects(boolean canCreateProjects) {
		this.canCreateProjects = canCreateProjects;
	}

	public abstract Authenticated authenticate(UsernamePasswordToken token) throws AuthenticationException;
	
}
