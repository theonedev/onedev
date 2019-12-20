package io.onedev.server.buildspec.job;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class Authentication implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userName;
	
	private String passwordSecret;

	@Editable(order=10000)
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=10100, description="Specify a project secret to be used as password")
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
		return Project.get().getBuildSetting().getHierarchySecrets(Project.get())
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
