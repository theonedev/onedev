package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.nio.file.Path;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.jspecify.annotations.Nullable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Password;

@Editable(name="Copilot API", description="""
	Use the local <tt>copilot-api</tt> proxy as a first-class backend for <tt>@copilot</tt>.
	This is useful when you want Copilot requests to route through the OpenAI-compatible proxy backed by GitHub OAuth/device auth.
	If Docker auto-start is enabled and you do not provide a GitHub token here, pre-authorize the mounted auth directory with the
	container auth flow first (for example by running the image with <tt>--auth</tt>).""")
public class CopilotApiSetting implements Serializable {

	private static final long serialVersionUID = 1L;

	private String endpoint = "http://127.0.0.1:4141/v1";

	private String model = "gpt-4.1";

	private boolean autoStart = true;

	private String projectPath = "/home/default/githubprojects/copilot-api";

	private String dockerImage = "onedev-copilot-api";

	private String containerName = "onedev-copilot-api";

	private String authDataDir = Path.of(System.getProperty("user.home"), ".local", "share", "copilot-api").toString();

	private String githubToken;

	private int startupTimeoutSeconds = 60;

	@Editable(order=100, name="Endpoint URL", description="""
		OpenAI-compatible endpoint exposed by <tt>copilot-api</tt>. The default matches the local Docker setup and includes the
		<tt>/v1</tt> prefix expected by OneDev model integrations.""")
	@Pattern(regexp="https?://.+", message="Endpoint URL should be a valid http/https URL")
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@Editable(order=200, name="Model Name", description="Model id to request from the Copilot API endpoint")
	@NotEmpty
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Editable(order=300, name="Auto Start via Docker", description="""
		Automatically build and start the local <tt>copilot-api</tt> Docker container when <tt>@copilot</tt> uses the
		Copilot API backend and the endpoint is not yet reachable.""")
	public boolean isAutoStart() {
		return autoStart;
	}

	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

	@Editable(order=400, name="Project Path", description="Local checkout of the copilot-api project used to build the Docker image")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@Editable(order=500, name="Docker Image", description="Docker image tag used for the locally built copilot-api image")
	@NotEmpty
	public String getDockerImage() {
		return dockerImage;
	}

	public void setDockerImage(String dockerImage) {
		this.dockerImage = dockerImage;
	}

	@Editable(order=600, name="Container Name", description="Container name used when auto-starting copilot-api")
	@NotEmpty
	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	@Editable(order=700, name="Auth Data Directory", description="""
		Host directory mounted to <tt>/root/.local/share/copilot-api</tt> inside the container.
		This is where OAuth/device-auth data is persisted between restarts.""")
	@NotEmpty
	public String getAuthDataDir() {
		return authDataDir;
	}

	public void setAuthDataDir(String authDataDir) {
		this.authDataDir = authDataDir;
	}

	@Editable(order=800, name="GitHub Token", description="""
		Optional GitHub token to pass as <tt>GH_TOKEN</tt> when starting the container.
		If left empty, OneDev expects the auth data directory to already contain a persisted token from the OAuth/device-auth flow.""")
	@Password
	@Nullable
	public String getGitHubToken() {
		return githubToken;
	}

	public void setGitHubToken(@Nullable String githubToken) {
		this.githubToken = githubToken;
	}

	@Editable(order=900, name="Startup Timeout (Seconds)", description="How long OneDev should wait for copilot-api to become reachable")
	@Min(value = 5, message = "Startup timeout should be at least 5 seconds")
	public int getStartupTimeoutSeconds() {
		return startupTimeoutSeconds;
	}

	public void setStartupTimeoutSeconds(int startupTimeoutSeconds) {
		this.startupTimeoutSeconds = startupTimeoutSeconds;
	}

}
