package io.onedev.server.web.editable.job.param;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class SecretEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String secret;

	@Editable
	@ChoiceProvider("getSecretChoices")
	@NotEmpty
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getSecretChoices() {
		return OneContext.get().getProject().getSecrets().stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
}
