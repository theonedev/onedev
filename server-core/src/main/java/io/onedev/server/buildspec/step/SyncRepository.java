package io.onedev.server.buildspec.step;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.SystemUtils;
import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable
@ClassValidating
public abstract class SyncRepository extends ServerSideStep implements Validatable {

	private static final long serialVersionUID = 1L;

	private String remoteUrl;
	
	private String userName;
	
	private String passwordSecret;
	
	private boolean withLfs;
	
	private boolean force;

	@Editable(order=100, name="Remote URL", description="Specify URL of remote git repository. "
			+ "Only http/https protocol is supported")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getRemoteUrl() {
		return remoteUrl;
	}

	@Editable(order=200)
	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@Editable(order=300, description="Optionally specify user name to access above repository")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Editable(order=400, name="Password / Access Token", 
			description="Specify a secret to be used as password or access token to access above repository")
	@ChoiceProvider("getPasswordSecretChoices")
	public String getPasswordSecret() {
		return passwordSecret;
	}

	public void setPasswordSecret(String passwordSecret) {
		this.passwordSecret = passwordSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getPasswordSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Editable(order=450, name="Transfer Git LFS Files", descriptionProvider="getLfsDescription")
	public boolean isWithLfs() {
		return withLfs;
	}

	public void setWithLfs(boolean withLfs) {
		this.withLfs = withLfs;
	}
	
	@SuppressWarnings("unused")
	private static String getLfsDescription() {
		if (!Bootstrap.isInDocker()) {
			return "If this option is enabled, git lfs command needs to be installed on OneDev server "
					+ "(even this step runs on other node)";
		} else {
			return null;
		}
	}

	@Editable(order=500, description="Whether or not use force option to overwrite changes in case ref updating "
			+ "can not be fast-forwarded")
	public boolean isForce() {
		return force;
	}

	public void setForce(boolean force) {
		this.force = force;
	}

	public String getRemoteUrlWithCredential(Build build) {
		String encodedPassword = null;
		if (getPasswordSecret() != null) {
			try {
				String password = build.getJobSecretAuthorizationContext().getSecretValue(getPasswordSecret());
				encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.name());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}

		String protocol = StringUtils.substringBefore(getRemoteUrl(), "//");
		String hostAndPath = StringUtils.substringAfter(getRemoteUrl(), "//");
		
		String remoteUrlWithCredentials = protocol + "//";
		
		if (getUserName() != null && encodedPassword != null)
			remoteUrlWithCredentials += getUserName() + ":" + encodedPassword + "@" + hostAndPath;
		else if (getUserName() != null)
			remoteUrlWithCredentials += getUserName() + "@" + hostAndPath;
		else if (encodedPassword != null)
			remoteUrlWithCredentials += encodedPassword + "@" + hostAndPath;
		else
			remoteUrlWithCredentials += hostAndPath;
		
		return remoteUrlWithCredentials;
	}
	
	protected Commandline newGit(Project project) {
		Commandline git = new Commandline(OneDev.getInstance(GitConfig.class).getExecutable());
		if (SystemUtils.IS_OS_MAC_OSX) {
			String path = System.getenv("PATH") + ":/usr/local/bin";
			git.environments().put("PATH", path);
		}
		git.workingDir(project.getGitDir());
		return git;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		if (getRemoteUrl() != null) {
			if (!getRemoteUrl().startsWith("http://") && !getRemoteUrl().startsWith("https://")) {
				isValid = false;
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("Only http(s) protocol is supported")
						.addPropertyNode("remoteUrl").addConstraintViolation();
			}
		}
		return isValid;
	}

}
