package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.jspecify.annotations.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.ServerConfig;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.ShowCondition;
import io.onedev.server.git.location.CurlLocation;
import io.onedev.server.git.location.GitLocation;
import io.onedev.server.git.location.SystemCurl;
import io.onedev.server.git.location.SystemGit;
import io.onedev.server.util.EditContext;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class SystemSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1;
	
	public static final String PROP_GIT_LOCATION = "gitLocation";
	
	public static final String PROP_CURL_LOCATION = "curlLocation";
	
	public static final String PROP_SSH_ROOT_URL = "sshRootUrl";

	public static final String PROP_SESSION_TIMEOUT = "sessionTimeout";
	
	public static final String PROP_USE_AVATAR_SERVICE = "useAvatarService";
	
	public static final String PROP_DISABLE_AUTO_UPDATE_CHECK = "disableAutoUpdateCheck";
	
	private String serverUrl;
	
	private String sshRootUrl;

	private GitLocation gitLocation = new SystemGit();
	
	private CurlLocation curlLocation = new SystemCurl();

	private int sessionTimeout = 30;

	private boolean disableAutoUpdateCheck;
	
	private boolean disableDashboard;
	
	private boolean useAvatarService;
	
	private String avatarServiceUrl = "https://secure.gravatar.com/avatar/";
	
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
	public static String deriveSshRootUrl(@Nullable String serverUrl) {
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
	public GitLocation getGitLocation() {
		return gitLocation;
	}

	public void setGitLocation(GitLocation gitLocation) {
		this.gitLocation = gitLocation;
	}

	@Editable(order=250, name="curl Command Line", description="OneDev configures git hooks to communicate with itself via curl")
	@Valid
	@NotNull(message="may not be empty")
	public CurlLocation getCurlLocation() {
		return curlLocation;
	}

	public void setCurlLocation(CurlLocation curlLocation) {
		this.curlLocation = curlLocation;
	}

	@Editable(order=300, description = "Specify web session timeout in minutes if no activity. Existing sessions will not be affected after changing this value.")
	@Min(value=1, message="Session timeout should be at least 1 minute")
	public int getSessionTimeout() {
		return sessionTimeout;
	}

	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	@Editable(order=400, description = "Auto update check is performed by requesting an image in " +
			"your browser from onedev.io indicating new version availability, with color " +
			"indicating severity of the update. It works the same way as how gravatar requests " +
			"avatar images. If disabled, you are highly recommended to check update manually " +
			"from time to time (can be done via help menu on left bottom of the screen) to see " +
			"if there are any security/critical fixes")
	public boolean isDisableAutoUpdateCheck() {
		return disableAutoUpdateCheck;
	}

	public void setDisableAutoUpdateCheck(boolean disableAutoUpdateCheck) {
		this.disableAutoUpdateCheck = disableAutoUpdateCheck;
	}

	@Editable(order=450)
	@ShowCondition("isDisableDashboardEnabled")
	public boolean isDisableDashboard() {
		return disableDashboard;
	}

	public void setDisableDashboard(boolean disableDashboard) {
		this.disableDashboard = disableDashboard;
	}
	
	@SuppressWarnings("unused")
	private static boolean isDisableDashboardEnabled() {
		return WicketUtils.isSubscriptionActive();
	}

	@Editable(order=500, description="Whether or not to use user avatar from a public service")
	public boolean isUseAvatarService() {
		return useAvatarService;
	}

	public void setUseAvatarService(boolean useAvatarService) {
		this.useAvatarService = useAvatarService;
	}

	@Editable(order=600, description="User avatar will be requested by appending a hash to this url")
	@DependsOn(property="useAvatarService")
	@NotEmpty
	public String getAvatarServiceUrl() {
		return avatarServiceUrl;
	}

	public void setAvatarServiceUrl(String avatarServiceUrl) {
		this.avatarServiceUrl = avatarServiceUrl;
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
				String errorMessage;
				if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://"))
					errorMessage = "Should start with either http:// or https://";
				else 
					errorMessage = "Malformed url";
				context.buildConstraintViolationWithTemplate(errorMessage)
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
