package io.onedev.server.web.component.build.secret;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class JobSecretsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<JobSecret> secrets = new ArrayList<>();

	@Editable
	public List<JobSecret> getSecrets() {
		return secrets;
	}

	public void setSecrets(List<JobSecret> secrets) {
		this.secrets = secrets;
	}
	
}
