package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import javax.validation.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.git.config.CurlConfig;
import io.onedev.server.git.config.GitConfig;
import io.onedev.server.git.config.SystemCurl;
import io.onedev.server.git.config.SystemGit;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class SystemSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1;
	
	private String serverUrl;
	
	private String sshRootUrl;

	private GitConfig gitConfig = new SystemGit();
	
	private CurlConfig curlConfig = new SystemCurl();
	
	private boolean gravatarEnabled;
	
	@Editable(name="Server URL", order=90, description="Specify root URL to access this server")
	@NotEmpty
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Editable(name="SSH Root URL", order=150, placeholderProvider="getSshRootUrlPlaceholder", description=""
			+ "Optionally specify SSH root URL, which will be used to construct project clone url via SSH protocol. "
			+ "Leave empty to derive from server url")
	public String getSshRootUrl() {
		return sshRootUrl;
	}

	public void setSshRootUrl(String sshRootUrl) {
		this.sshRootUrl = sshRootUrl;
	}
	
	@SuppressWarnings("unused")
	private static String getSshRootUrlPlaceholder() {
		return deriveSshRootUrl((String) EditContext.get().getInputValue("serverUrl"));
	}

	@Nullable
	private static String deriveSshRootUrl(@Nullable String serverUrl) {
		if (serverUrl != null) {
			try {
				URL url = new URL(serverUrl);
				if (StringUtils.isNotBlank(url.getHost())) {
					if (url.getPort() == 80 || url.getPort() == 443 || url.getPort() == -1) {
						return "ssh://" + url.getHost();
					} else {
						ServerConfig serverConfig = OneDev.getInstance(ServerConfig.class);
						return "ssh://" + url.getHost() + ":" + serverConfig.getSshPort();
					}
				}
			} catch (MalformedURLException e) {
			}
		}
		return null;
	}

	@Editable(order=200, name="Git Command Line", description="OneDev requires git command line to manage repositories. The minimum "
			+ "required version is 2.11.1. Also make sure that git-lfs is installed if you want to retrieve "
			+ "LFS files in build job")
	@Valid
	@NotNull(message="may not be empty")
	public GitConfig getGitConfig() {
		return gitConfig;
	}

	public void setGitConfig(GitConfig gitConfig) {
		this.gitConfig = gitConfig;
	}

	@Editable(order=250, name="curl Command Line", description="OneDev configures git hooks to communicate with itself via curl")
	@Valid
	@NotNull(message="may not be empty")
	public CurlConfig getCurlConfig() {
		return curlConfig;
	}

	public void setCurlConfig(CurlConfig curlConfig) {
		this.curlConfig = curlConfig;
	}
	
	@Editable(order=500, description="Whether or not to enable user gravatar (https://gravatar.com)")
	public boolean isGravatarEnabled() {
		return gravatarEnabled;
	}

	public void setGravatarEnabled(boolean gravatarEnabled) {
		this.gravatarEnabled = gravatarEnabled;
	}

	public String getEffectiveSshRootUrl() {
		if (getSshRootUrl() != null)
			return getSshRootUrl();
		else
			return Preconditions.checkNotNull(deriveSshRootUrl(getServerUrl()));
	}
	
    public String getSshServerName() {
    	String temp = getEffectiveSshRootUrl();
    	int index = temp.indexOf("://");
    	if (index != -1)
    		temp = temp.substring(index+3);
    	index = temp.indexOf(':');
    	if (index != -1)
    		temp = temp.substring(0, index);
    	return StringUtils.stripEnd(temp, "/\\");
    }
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;
		if (serverUrl != null) {
			serverUrl = StringUtils.stripEnd(serverUrl, "/\\");
			try {
				URL url = new URL(serverUrl);
				if (StringUtils.isBlank(url.getProtocol())) {
					context.buildConstraintViolationWithTemplate("Protocol is not specified")
							.addPropertyNode("serverUrl").addConstraintViolation();
					isValid = false;
				} else if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
					context.buildConstraintViolationWithTemplate("Protocol should be either http or https")
							.addPropertyNode("serverUrl").addConstraintViolation();
					isValid = false;
				}
				if (StringUtils.isBlank(url.getHost())) {
					context.buildConstraintViolationWithTemplate("Host is not specified")
							.addPropertyNode("serverUrl").addConstraintViolation();
					isValid = false;
				}
				if (StringUtils.isNotBlank(url.getPath())) {
					context.buildConstraintViolationWithTemplate("Path should not be specified")
							.addPropertyNode("serverUrl").addConstraintViolation();
					isValid = false;
				}
			} catch (MalformedURLException e) {
				context.buildConstraintViolationWithTemplate("Malformed url")
						.addPropertyNode("serverUrl").addConstraintViolation();
				isValid = false;
			}
		}
		if (sshRootUrl != null) {
			sshRootUrl = StringUtils.stripEnd(sshRootUrl, "/\\");
			if (!sshRootUrl.startsWith("ssh://")) {
				context.buildConstraintViolationWithTemplate("This url should start with ssh://")
						.addPropertyNode("sshRootUrl").addConstraintViolation();
				isValid = false;
			}
		}
		
		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}
	
}
