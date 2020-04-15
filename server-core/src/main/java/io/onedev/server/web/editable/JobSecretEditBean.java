package io.onedev.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class JobSecretEditBean implements Serializable {

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
		List<String> secretNames = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		for (JobSecret secret: page.getProject().getBuildSetting().getJobSecrets()) 
			secretNames.add(secret.getName());
		return secretNames;
	}
	
}
