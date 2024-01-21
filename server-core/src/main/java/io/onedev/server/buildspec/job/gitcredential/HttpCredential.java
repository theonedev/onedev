package io.onedev.server.buildspec.job.gitcredential;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;

import javax.validation.constraints.NotEmpty;

import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.HttpCloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.web.UrlManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.validation.Validatable;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.ChoiceProvider;
import io.onedev.server.annotation.Editable;

@Editable(name="HTTP(S)", order=200)
@ClassValidating
public class HttpCredential implements GitCredential, Validatable {

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
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public CloneInfo newCloneInfo(Build build, String jobToken) {
		return new HttpCloneInfo(OneDev.getInstance(UrlManager.class).cloneUrlFor(build.getProject(), false), 
				build.getJobAuthorizationContext().getSecretValue(accessTokenSecret));
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (!Project.get().getHierarchyJobSecrets().stream()
				.anyMatch(it->it.getName().equals(accessTokenSecret))) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Secret not found (" + accessTokenSecret + ")")
					.addPropertyNode("accessTokenSecret")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
