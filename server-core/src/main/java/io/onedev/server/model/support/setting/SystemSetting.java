package io.onedev.server.model.support.setting;

import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.git.config.CurlConfig;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.git.config.SystemCurl;
import io.onedev.server.git.config.SystemGit;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.util.validation.annotation.Directory;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class SystemSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1;

	private String serverUrl;
	
	private String storagePath;
	
	private GitConfig gitConfig = new SystemGit();
	
	private CurlConfig curlConfig = new SystemCurl();
	
	private boolean gravatarEnabled = true;
	
	@Editable(name="Server URL", order=90, description="Specify root URL to access this server. OneDev uses this url "
			+ "to construct various links in notification email.")
	@NotEmpty
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Editable(name="Storage Directory", order=100, description="Specify directory to store OneDev data such as Git projects.")
	@Directory(absolute=true, outsideOfInstallDir=true, writeable=true)
	@NotEmpty
	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	@Editable(order=200, description="OneDev requires git command line to manage repositories. The minimum "
			+ "required version is 1.8.0.")
	@Valid
	@NotNull(message="may not be empty")
	public GitConfig getGitConfig() {
		return gitConfig;
	}

	public void setGitConfig(GitConfig gitConfig) {
		this.gitConfig = gitConfig;
	}

	@Editable(order=250, description="OneDev configures git hooks to communicate with itself via curl")
	@Valid
	@NotNull(message="may not be empty")
	public CurlConfig getCurlConfig() {
		return curlConfig;
	}

	public void setCurlConfig(CurlConfig curlConfig) {
		this.curlConfig = curlConfig;
	}

	@Editable(order=300, description="Whether or not to enable user gravatar.")
	public boolean isGravatarEnabled() {
		return gravatarEnabled;
	}

	public void setGravatarEnabled(boolean gravatarEnabled) {
		this.gravatarEnabled = gravatarEnabled;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (serverUrl != null)
			serverUrl = StringUtils.stripEnd(serverUrl, "/\\");
		return true;
	}

}
