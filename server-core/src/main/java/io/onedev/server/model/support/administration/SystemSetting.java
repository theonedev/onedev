package io.onedev.server.model.support.administration;

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
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class SystemSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1;

	private String serverUrl;

	private GitConfig gitConfig = new SystemGit();
	
	private CurlConfig curlConfig = new SystemCurl();
	
	private boolean gravatarEnabled;
	
	private int cpu;
	
	private int memory;
	
	@SuppressWarnings("restriction")
	public SystemSetting() {
		cpu = Runtime.getRuntime().availableProcessors()*1000;
		
		com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean)
			     java.lang.management.ManagementFactory.getOperatingSystemMXBean();
		memory = (int)(os.getTotalPhysicalMemorySize()/1024/1024);				
	}
	
	@Editable(name="Server URL", order=90, description="Specify root URL to access this server. Build jobs "
			+ "running in Kubernetes cluster will access this url to download source and artifacts")
	@NotEmpty
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	@Editable(order=200, description="OneDev requires git command line to manage repositories. The minimum "
			+ "required version is 2.11.1")
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

	@Editable(order=300, description="Whether or not to enable user gravatar (https://gravatar.com)")
	public boolean isGravatarEnabled() {
		return gravatarEnabled;
	}

	public void setGravatarEnabled(boolean gravatarEnabled) {
		this.gravatarEnabled = gravatarEnabled;
	}

	@Editable(order=400, name="Server CPU", description="Specify CPU capability in millis available to run builds "
			+ "on server (via server docker/shell executor). This is normally <i>(CPU cores)*1000</i>, for "
			+ "instance <i>4000</i> means 4 CPU cores")
	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	@Editable(order=500, name="Server Memory", description="Specify physical memory in mega bytes available to run "
			+ "builds on server (via server docker/shell executor)")
	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (serverUrl != null)
			serverUrl = StringUtils.stripEnd(serverUrl, "/\\");
		return true;
	}
}
