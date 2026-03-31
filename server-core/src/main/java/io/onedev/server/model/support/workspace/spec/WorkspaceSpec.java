package io.onedev.server.model.support.workspace.spec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.io.FilenameUtils;

import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.ClassValidating;
import io.onedev.server.annotation.DependsOn;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.annotation.SuggestionProvider;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.model.support.workspace.spec.shell.DefaultShell;
import io.onedev.server.model.support.workspace.spec.shell.WorkspaceShell;
import io.onedev.server.service.SettingService;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.util.SuggestionUtils;

@Editable
@ClassValidating
public class WorkspaceSpec implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;

	public static final String PROP_NAME = "name";

	public static final String PROP_CACHE_CONFIGS = "cacheConfigs";

	public static final String PROP_USER_DATAS = "userDatas";

	public static final String PROP_CONFIG_FILES = "configFiles";

	private String name;

	private String provisioner;

	private boolean runInContainer;

	private String image;

	private WorkspaceShell shell = new DefaultShell();

	private List<EnvVar> envVars = new ArrayList<>();

	private List<ConfigFile> configFiles = new ArrayList<>();

	private List<ShortcutConfig> shortcutConfigs = new ArrayList<>();

	private List<UserData> userDatas = new ArrayList<>();

	private List<CacheConfig> cacheConfigs = new ArrayList<>();

	private boolean retrieveLfs;

	private String runAs;
	
	private List<Integer> containerPorts = new ArrayList<>();

	private List<RegistryLogin> registryLogins = new ArrayList<>();

	@Editable(order = 50, description = "Specify a name to identify this workspace spec")
	@SuggestionProvider("getNameSuggestions")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		Project project = Project.get();
		if (project != null && project.getParent() != null) {
			List<String> candidates = new ArrayList<>();
			for (var spec : project.getParent().getHierarchyWorkspaceSpecs())
				candidates.add(spec.getName());
			project.getWorkspaceSpecs().forEach(it -> candidates.remove(it.getName()));
			return SuggestionUtils.suggestOverrides(candidates, status);
		} else {
			return new ArrayList<>();
		}
	}

	@Editable(order=55, placeholderProvider="getProvisionerPlaceholder", descriptionProvider="getProvisionerDescription")
	@Interpolative(literalSuggester="suggestProvisioners", variableSuggester="suggestVariables")
	public String getProvisioner() {
		return provisioner;
	}

	public void setProvisioner(String provisioner) {
		this.provisioner = provisioner;
	}

	@SuppressWarnings("unused")
	private static String getProvisionerPlaceholder() {
		if (OneDev.getInstance(SettingService.class).getWorkspaceProvisioners().isEmpty())
			return "Auto-discovered provisioner";
		else
			return "First applicable provisioner";
	}

	@SuppressWarnings("unused")
	private static String getProvisionerDescription() {
		if (OneDev.getInstance(SettingService.class).getWorkspaceProvisioners().isEmpty())
			return "Optionally specify provisioner for this workspace spec. Leave empty to use auto-discovered provisioner";
		else
			return "Optionally specify provisioner for this workspace spec. Leave empty to use first applicable provisioner";
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProvisioners(String matchWith) {
		List<String> provisionerNames = new ArrayList<>();
		for (WorkspaceProvisioner provisioner: OneDev.getInstance(SettingService.class).getWorkspaceProvisioners()) {
			if (provisioner.isEnabled())
				provisionerNames.add(provisioner.getName());
		}
		return SuggestionUtils.suggest(provisionerNames, matchWith);
	}

	@Editable(order = 100, description = """
		Whether or not to create the workspace inside container. If enabled, you will need a <a href='https://onedev.io/pricing' target='_blank'>enterprise subscription</a>""")
	public boolean isRunInContainer() {
		return runInContainer;
	}

	public void setRunInContainer(boolean runInContainer) {
		this.runInContainer = runInContainer;
	}

	@Editable(order = 200, name = "container:image", description = """
		Specify container image to create workspace inside. Note that this image should 
		have <a href='https://git-scm.com' target='_blank'>git</a>, <a href='https://git-lfs.com' target='_blank'>git-lfs</a>, <a href='https://curl.se' target='_blank'>curl</a>, and <a href='https://github.com/tmux/tmux' target='_blank'>tmux</a> installed
		""")
	@Interpolative(variableSuggester="suggestVariables")
	@DependsOn(property = "runInContainer")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order = 300, description = """
		Configure shell of the workspace. It will be used to launch workspace terminal, run workspace setup commands etc
		""")
	@NotNull
	public WorkspaceShell getShell() {
		return shell;
	}

	public void setShell(WorkspaceShell shell) {
		this.shell = shell;
	}

	@Editable(order = 350, name = "User Data", description = """
			Optionally define user data for the workspace. User data populated by a workspace will be uploaded 
			after the workspace is deleted, and can be reused by other workspaces created by same user""")
	@DependsOn(property = "runInContainer")
	@Valid
	public List<UserData> getUserDatas() {
		return userDatas;
	}

	public void setUserDatas(List<UserData> userDatas) {
		this.userDatas = userDatas;
	}

	@Editable(order = 400, name = "Environment Variables", description = "Optionally specify environment variables")
	@Valid
	public List<EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
	}

	@Editable(order = 600, name = "Shortcuts", description = """
		Optionally specify list of shortcuts at top of workspace. When workspace is created, 
		the first shortcut will be opened automatically""")
	@Valid
	public List<ShortcutConfig> getShortcutConfigs() {
		return shortcutConfigs;
	}

	public void setShortcutConfigs(List<ShortcutConfig> shortcutConfigs) {
		this.shortcutConfigs = shortcutConfigs;
	}
	
	@Editable(order = 1100, name = "Retrieve LFS Files", group = "More Settings", description = "Enable this to retrieve Git LFS files")
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
	@Interpolative(variableSuggester="suggestVariables")
	@DependsOn(property = "runInContainer")
	@Pattern(regexp = "(\\d+:\\d+)?", message = "Should be specified in form of <uid>:<gid>")
	public String getRunAs() {
		return runAs;
	}

	public void setRunAs(String runAs) {
		this.runAs = runAs;
	}

	@Editable(order = 1500, group = "More Settings", description = """
			Optionally specify registry logins to override those defined in workspace provisioner. 
			For built-in registry, use <code>@server_url@</code> for registry url, 
			<code>@workspace_token@</code> for user name, and access token for password
			""")
	@DependsOn(property = "runInContainer")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Editable(order = 1550, group = "More Settings", description = "Optionally define config files for the workspace")
	@DependsOn(property = "runInContainer")
	@Valid
	public List<ConfigFile> getConfigFiles() {
		return configFiles;
	}

	public void setConfigFiles(List<ConfigFile> configFiles) {
		this.configFiles = configFiles;
	}

	@Editable(order = 1600, group = "More Settings", name = "Caches", description = """
			Optionally define caches for the workspace. Cache populated by a workspace will be uploaded 
			after the workspace is deleted, and can be used by other workspaces created after it""")
	@Valid
	public List<CacheConfig> getCacheConfigs() {
		return cacheConfigs;
	}

	public void setCacheConfigs(List<CacheConfig> cacheConfigs) {
		this.cacheConfigs = cacheConfigs;
	}

	@Editable(order = 1700, group = "More Settings", name="Exposed Ports", placeholder = "No exposed ports", description = """
			Optionally specify container ports to expose. These ports will be mapped to random ports on host 
			which will be displayed on the workspace page""")
	@DependsOn(property = "runInContainer")
	public List<Integer> getContainerPorts() {
		return containerPorts;
	}

	public void setContainerPorts(List<Integer> containerPorts) {
		this.containerPorts = containerPorts;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;

		for (int i = 0; i < cacheConfigs.size(); i++) {
			var cacheConfig = cacheConfigs.get(i);
			for (int j = 0; j < cacheConfig.getPaths().size(); j++) {
				var entry = cacheConfig.getPaths().get(j);
				if (!entry.contains("@")) {
					if (!isRunInContainer() && FilenameUtils.getPrefixLength(entry) > 0) {
						context.buildConstraintViolationWithTemplate(""
									+ "Item #" + (i + 1) + ": absolute path '" 
									+ entry + "' is not allowed when not running in container")
								.addPropertyNode(PROP_CACHE_CONFIGS).addConstraintViolation();
						isValid = false;
					}
				}
			}
		}

		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}

	protected static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

}
