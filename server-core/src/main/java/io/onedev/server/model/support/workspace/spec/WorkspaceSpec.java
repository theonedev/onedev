package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.buildspec.job.EnvVar;

@Editable
public class WorkspaceSpec implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private boolean runInContainer;

	private String image;

	private String shell;

	private ArrayList<EnvVar> envVars = new ArrayList<>();

	private ArrayList<UserConfig> userConfigs = new ArrayList<>();

	private boolean retrieveLfs;

	private String runAs;

	private ArrayList<RegistryLogin> registryLogins = new ArrayList<>();

	@Editable(order = 50, description = "Specify a name to identify this workspace spec")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order = 100, description = "Whether or not to run this environment inside container")
	public boolean isRunInContainer() {
		return runInContainer;
	}

	public void setRunInContainer(boolean runInContainer) {
		this.runInContainer = runInContainer;
	}

	@Editable(order = 200, name = "container:image", description = "Specify container image to execute commands inside")
	@DependsOn(property = "runInContainer")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order = 300, name = "Shell", placeholder = "default", description = "Specify shell to be used. Leave empty to use default shell (cmd for Windows, sh for others)")
	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}

	@Editable(order = 400, name = "Environment Variables", description = "Optionally specify environment variables")
	@Valid
	public ArrayList<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(ArrayList<EnvVar> envVars) {
		this.envVars = envVars;
	}

	@Editable(order = 500, name = "Config Files", description = """
			In order not to interfere with each other, each workspace has its own user home. \
			This setting populates user home with desired config files""")
	@Valid
	public ArrayList<UserConfig> getUserConfigs() {
		return userConfigs;
	}

	public void setUserConfigs(ArrayList<UserConfig> userConfigs) {
		this.userConfigs = userConfigs;
	}

	@Editable(order = 1100, name = "Retrieve LFS Files", description = "Enable this to retrieve Git LFS files")
	public boolean isRetrieveLfs() {
		return retrieveLfs;
	}

	public void setRetrieveLfs(boolean retrieveLfs) {
		this.retrieveLfs = retrieveLfs;
	}

	@Editable(order = 1400, name = "Run As", group = "More Settings", placeholder = "root", description = """
			Optionally specify uid:gid to run container as. \
			<b class='text-warning'>Note:</b> This setting should be left empty if container runtime is rootless or \
			using user namespace remapping
			""")
	@DependsOn(property = "runInContainer")
	@Pattern(regexp = "(\\d+:\\d+)?", message = "Should be specified in form of <uid>:<gid>")
	public String getRunAs() {
		return runAs;
	}

	public void setRunAs(String runAs) {
		this.runAs = runAs;
	}

	@Editable(order = 1500, group = "More Settings", description = """
			Optionally specify registry logins to override those defined in workspace provisioner. \
			For built-in registry, use <code>@server_url@</code> for registry url, \
			<code>@workspace_token@</code> for user name, and access token for password
			""")
	@DependsOn(property = "runInContainer")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(ArrayList<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

}
