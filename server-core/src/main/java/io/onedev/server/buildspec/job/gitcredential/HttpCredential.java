package io.onedev.server.buildspec.job.gitcredential;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.HttpCloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="HTTP(S)", order=200)
public class HttpCredential implements GitCredential {

	private static final long serialVersionUID = 1L;

	private String accessTokenSecret;

	@Editable(order=200, description="Specify a secret to be used as access token")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@NotEmpty
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getBuildSetting().getJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public CloneInfo newCloneInfo(Build build, String jobToken) {
		return new HttpCloneInfo(OneDev.getInstance(UrlManager.class).cloneUrlFor(build.getProject(), false), 
				build.getSecretValue(accessTokenSecret));
	}

}
