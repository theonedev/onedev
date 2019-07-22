package io.onedev.server.model.support;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Password;

@Editable
public class RegistryLogin implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String registryUrl;
	
	private String userName;
	
	private String password;

	@Editable(order=100, description="Specify registry url. Leave empty for official registry")
	@NameOfEmptyValue("Default Registry")
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
	
}