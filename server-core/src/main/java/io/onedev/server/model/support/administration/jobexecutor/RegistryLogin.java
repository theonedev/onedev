package io.onedev.server.model.support.administration.jobexecutor;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.onedev.agent.job.RegistryLoginFacade;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;

@Editable
public class RegistryLogin implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String registryUrl;
	
	private String userName;
	
	private String password;

	@Editable(order=100, placeholder="Default registry", 
			description="Specify registry url. Leave empty for official registry")
	public String getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	@Editable(order=200)
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=300)
	@NotEmpty
	@Password
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public RegistryLoginFacade getFacade() {
		var registryUrl = getRegistryUrl();
		if (registryUrl == null)
			registryUrl = "https://index.docker.io/v1/";
		return new RegistryLoginFacade(registryUrl, getUserName(), getPassword());
	}
}