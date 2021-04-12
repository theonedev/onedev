package io.onedev.server.buildspec.job.gitcredential;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;

import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.k8shelper.CloneInfo;
import io.onedev.k8shelper.SshCloneInfo;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.SshSetting;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(name="SSH", order=300)
@ClassValidating
public class SshCredential implements GitCredential, Validatable {

	private static final long serialVersionUID = 1L;

	private String keySecret;

	@Editable(order=100, description="Specify a secret to be used as SSH private key")
	@ChoiceProvider("getKeySecretChoices")
	@NotEmpty
	public String getKeySecret() {
		return keySecret;
	}

	public void setKeySecret(String keySecret) {
		this.keySecret = keySecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getKeySecretChoices() {
		return Project.get().getBuildSetting().getJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public CloneInfo newCloneInfo(Build build, String jobToken) {
		String cloneUrl = OneDev.getInstance(UrlManager.class).cloneUrlFor(build.getProject(), true);
		SshSetting sshSetting = OneDev.getInstance(SettingManager.class).getSshSetting();
		StringBuilder knownHosts = new StringBuilder(sshSetting.getServerName()).append(" ");
		try {
			PublicKeyEntry.appendPublicKeyEntry(knownHosts, sshSetting.getPublicKey());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new SshCloneInfo(cloneUrl, build.getSecretValue(keySecret), knownHosts.toString());
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (!Project.get().getBuildSetting().getJobSecrets().stream()
				.anyMatch(it->it.getName().equals(keySecret))) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Secret not found (" + keySecret + ")")
					.addPropertyNode("keySecret")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
