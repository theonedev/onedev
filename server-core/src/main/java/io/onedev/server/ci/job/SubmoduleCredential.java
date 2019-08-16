package io.onedev.server.ci.job;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SubmoduleCredential implements Serializable {

	private static final long serialVersionUID = 1L;

	private String url;
	
	private String userName;
	
	private String passwordSecret;

	@Editable(order=100)
	@NotEmpty
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Editable(order=200)
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=300, description="Specify a project secret to be used as password")
	@ChoiceProvider("getPasswordSecretChoices")
	@NotEmpty
	public String getPasswordSecret() {
		return passwordSecret;
	}

	public void setPasswordSecret(String passwordSecret) {
		this.passwordSecret = passwordSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getPasswordSecretChoices() {
		return OneContext.get().getProject().getSecrets().stream().map(it->it.getName()).collect(Collectors.toList());
	}

}