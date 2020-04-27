package io.onedev.server.model.support.administration.authenticator;

import java.io.Serializable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public abstract class Authenticator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int timeout = 300;

	@Editable(order=10000, description="Specify network timeout in seconds when authenticate through this system")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public abstract Authenticated authenticate(UsernamePasswordToken token) throws AuthenticationException;
	
	public abstract boolean isManagingMemberships();
	
	public abstract boolean isManagingSshKeys();
	
}
