package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.GroupChoice;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.service.GroupService;
import io.onedev.server.model.Group;
import io.onedev.server.util.usage.Usage;

@Editable
public class SecuritySetting implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SecuritySetting.class);

	private boolean enableAnonymousAccess = false;
	
	private boolean enableSelfRegister = true;
	
	private String allowedSelfRegisterEmailDomain;
	
	private boolean enableSelfDeregister;
	
	private PasswordPolicy passwordPolicy;

	private String defaultGroupName;
	
	private boolean enforce2FA;
	
	private List<String> corsAllowedOrigins = new ArrayList<>();
	
	@Editable(order=100, description="Whether or not to allow anonymous users to access this server")
	public boolean isEnableAnonymousAccess() {
		return enableAnonymousAccess;
	}

	public void setEnableAnonymousAccess(boolean enableAnonymousAccess) {
		this.enableAnonymousAccess = enableAnonymousAccess;
	}

	@Editable(order=200, name="Enable Account Self Sign-Up", description="User can sign up if this option is enabled")
	public boolean isEnableSelfRegister() {
		return enableSelfRegister;
	}

	public void setEnableSelfRegister(boolean enableSelfRegister) {
		this.enableSelfRegister = enableSelfRegister;
	}

	@Editable(order=225, name="Allowed Self Sign-Up Email Domain", placeholder = "Any domain", description = "Optionally specify allowed email domain for self sign-up users. Use '*' or '?' for pattern match")
	@Patterns
	@DependsOn(property="enableSelfRegister")
	public String getAllowedSelfRegisterEmailDomain() {
		return allowedSelfRegisterEmailDomain;
	}

	public void setAllowedSelfRegisterEmailDomain(String allowedSelfRegisterEmailDomain) {
		this.allowedSelfRegisterEmailDomain = allowedSelfRegisterEmailDomain;
	}
	
	@Editable(order=250, name="Enforce Password Policy", description="Enforce password policy for new users")
	public PasswordPolicy getPasswordPolicy() {
		return passwordPolicy;
	}

	public void setPasswordPolicy(PasswordPolicy passwordPolicy) {
		this.passwordPolicy = passwordPolicy;
	}

	@Editable(order=300, name="Default Group", description="Optionally add new users to specified default group")
	@GroupChoice
	public String getDefaultGroupName() {
		return defaultGroupName;
	}

	public void setDefaultGroupName(String defaultGroupName) {
		this.defaultGroupName = defaultGroupName;
	}

	@Editable(order=350, name="Enable Account Self Removal", description = "Whether or not user " +
			"can remove own account")
	public boolean isEnableSelfDeregister() {
		return enableSelfDeregister;
	}

	public void setEnableSelfDeregister(boolean enableSelfDeregister) {
		this.enableSelfDeregister = enableSelfDeregister;
	}

	@Editable(order=400, name="Enforce Two-factor Authentication", description="Check this to enforce two-factor authentication for all users in the system")
	public boolean isEnforce2FA() {
		return enforce2FA;
	}

	public void setEnforce2FA(boolean enforce2FA) {
		this.enforce2FA = enforce2FA;
	}

	@Editable(order=500, name="CORS Allowed Origins", placeholder = "Input allowed CORS origin, hit ENTER to add", description = "" +
			"Optionally specify allowed <a href='https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS' target='_blank'>CORS</a> origins. " +
			"For a CORS simple or preflight request, if value of request header <code>Origin</code> is included here, " +
			"the response header <code>Access-Control-Allow-Origin</code> will be set to the same value")
	@NotNull
	public List<String> getCorsAllowedOrigins() {
		return corsAllowedOrigins;
	}

	public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
		this.corsAllowedOrigins = corsAllowedOrigins;
	}

	@Nullable
	public Group getDefaultGroup() {
		if (defaultGroupName != null) {
       		Group group = OneDev.getInstance(GroupService.class).find(defaultGroupName);
       		if (group == null) 
       			logger.error("Unable to find default group: " + defaultGroupName);
       		else
       			return group;
		}
		return null;
	}
	
	public void onRenameGroup(String oldName, String newName) {
		if (oldName.equals(defaultGroupName))
			defaultGroupName = newName;
	}
	
	public Usage onDeleteGroup(String groupName) {
		Usage usage = new Usage();
		if (groupName.equals(defaultGroupName))
			usage.add("default group for sign-in users");
		return usage.prefix("security settings");
	}
	
}
