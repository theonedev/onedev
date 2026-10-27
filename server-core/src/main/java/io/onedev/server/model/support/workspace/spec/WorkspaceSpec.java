package io.onedev.server.model.support.workspace.spec;

import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.commons.codeassist.InputCompletion;
import io.onedev.commons.codeassist.InputStatus;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.*;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.workspaceprovisioner.WorkspaceProvisioner;
import io.onedev.server.model.support.workspace.spec.shell.PosixShell;
import io.onedev.server.model.support.workspace.spec.shell.WorkspaceShell;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.usage.Usage;
import io.onedev.server.validation.Validatable;
import io.onedev.server.web.util.SuggestionUtils;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

@Editable
@ClassValidating
public class WorkspaceSpec implements Serializable, Validatable {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String PROP_NAME = "name";

	public static final String PROP_CACHE_CONFIGS = "cacheConfigs";

	public static final String PROP_USER_DATAS = "userDatas";

	public static final String PROP_CONFIG_FILES = "configFiles";

	private String name;

	private String description;

	private ProvisionMode provisionMode = ProvisionMode.DOCKER;

	private String provisioner;

	private String image;

	private String template;

	private WorkspaceShell shell = new PosixShell();

	private List<EnvVar> envVars = new ArrayList<>();

	private List<ConfigFile> configFiles = new ArrayList<>();

	private List<ShortcutConfig> shortcutConfigs = new ArrayList<>();

	private List<UserData> userDatas = new ArrayList<>();

	private List<CacheConfig> cacheConfigs = new ArrayList<>();

	private boolean retrieveLfs = true;

	private boolean retrieveSubmodules = true;

	private String runAs = "0:0";
	
	private List<Integer> exposedPorts = new ArrayList<>();

	private List<RegistryLogin> registryLogins = new ArrayList<>();

	private TaskAutomation taskAutomation;

	@Editable(order = 50, description = "Specify a name to identify this workspace spec")
	@SuggestionProvider("getNameSuggestions")
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order = 52, placeholder = "No description", description = "Optionally describe this workspace spec")
	@Multiline
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@SuppressWarnings("unused")
	private static List<InputCompletion> getNameSuggestions(InputStatus status) {
		Project project = Project.get();
		if (project != null && project.getSettingsParent() != null) {
			List<String> candidates = new ArrayList<>();
			for (var spec : project.getSettingsParent().getHierarchyWorkspaceSpecs())
				candidates.add(spec.getName());
			project.getWorkspaceSpecs().forEach(it -> candidates.remove(it.getName()));
			return SuggestionUtils.suggestOverrides(candidates, status);
		} else {
			return new ArrayList<>();
		}
	}

	@Editable(order = 55, description = "Specify whether to provision the workspace in a sandbox, Docker container, or directly on the host (barebone)")
	@NotNull
	public ProvisionMode getProvisionMode() {
		return provisionMode;
	}

	public void setProvisionMode(ProvisionMode provisionMode) {
		this.provisionMode = provisionMode;
	}

	@Editable(order=100, placeholderProvider="getProvisionerPlaceholder", descriptionProvider="getProvisionerDescription")
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
		var provisionMode = (ProvisionMode) EditContext.get().getInputValue("provisionMode");
		for (WorkspaceProvisioner provisioner: OneDev.getInstance(SettingService.class).getWorkspaceProvisioners()) {
			if (provisioner.isEnabled() && provisioner.getProvisionMode() == provisionMode
					&& provisioner.isApplicable(Project.get()))
				provisionerNames.add(provisioner.getName());
		}
		return SuggestionUtils.suggest(provisionerNames, matchWith);
	}

	@Editable(order = 60, name = "container:image", description = """
		Specify container image to create workspace inside. The image must have \
		shell, Git, and tmux installed""")
	@Interpolative(variableSuggester="suggestVariables")
	@DependsOn(property = "provisionMode", value = "DOCKER")
	@NotEmpty
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Editable(order = 70, description = """
		Specify sandbox template image to create workspace from. The image must \
		have shell, Git, Python 3, and tmux installed""")
	@Interpolative(variableSuggester="suggestVariables")
	@DependsOn(property = "provisionMode", value = "SANDBOX")
	@NotEmpty
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@Editable(order = 300, description = """
		Configure shell of the workspace. It will be used to launch workspace terminal, run workspace setup commands etc
		""")
	@Valid
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
	@DependsOn(property = "provisionMode", value = "HOST", inverse = true)
	public List<@Valid UserData> getUserDatas() {
		return userDatas;
	}

	public void setUserDatas(List<UserData> userDatas) {
		this.userDatas = userDatas;
	}

	@Editable(order = 375, name="Exposed Ports", placeholder = "No exposed ports", description = """
			Optionally specify workspace ports to expose. These ports will be mapped to random ports on host
			which will be displayed on the workspace page""")
	@DependsOn(property = "provisionMode", value = "HOST", inverse = true)
	@NotNull
	public List<@NotNull Integer> getExposedPorts() {
		return exposedPorts;
	}

	public void setExposedPorts(List<Integer> exposedPorts) {
		this.exposedPorts = exposedPorts;
	}

	@Editable(order = 400, name = "Environment Variables", description = "Optionally specify environment variables")
	@NotNull
	public List<@NotNull @Valid EnvVar> getEnvVars() {
		return envVars;
	}

	public void setEnvVars(List<EnvVar> envVars) {
		this.envVars = envVars;
	}

	@Editable(order = 600, name = "Shortcuts", description = """
		Optionally specify list of shortcuts at top of workspace. When workspace is created, 
		the first shortcut will be opened automatically""")
	@NotNull
	public List<@NotNull @Valid ShortcutConfig> getShortcutConfigs() {
		return shortcutConfigs;
	}

	public void setShortcutConfigs(List<ShortcutConfig> shortcutConfigs) {
		this.shortcutConfigs = shortcutConfigs;
	}

	@Editable(order = 650, placeholder = "Not configured", description = """
			Enable this if the spec can be used by AI user to create workspaces to run assigned tasks""")
	@Valid
	public TaskAutomation getTaskAutomation() {
		return taskAutomation;
	}

	public void setTaskAutomation(TaskAutomation taskAutomation) {
		this.taskAutomation = taskAutomation;
	}
	
	@Editable(order = 1100, name = "Retrieve LFS Files", group = "More Settings", description = "Enable this to retrieve Git LFS files")
	public boolean isRetrieveLfs() {
		return retrieveLfs;
	}

	public void setRetrieveLfs(boolean retrieveLfs) {
		this.retrieveLfs = retrieveLfs;
	}

	@Editable(order = 1200, name = "Retrieve Submodules", group = "More Settings", description = """
			Whether or not to retrieve submodules. Refer to
			<a href='${docsite}/tutorials/cicd/clone-submodules' target='_blank'>this tutorial</a> on
			how to configure submodule authentication
			""")
	public boolean isRetrieveSubmodules() {
		return retrieveSubmodules;
	}

	public void setRetrieveSubmodules(boolean retrieveSubmodules) {
		this.retrieveSubmodules = retrieveSubmodules;
	}

	@Editable(order = 1400, name = "Run As", group = "More Settings", description = "Specify uid:gid to run container as")
	@Interpolative(variableSuggester="suggestVariables")
	@DependsOn(property = "provisionMode", value = "DOCKER")
	@Pattern(regexp = "(\\d+:\\d+)?", message = "Should be specified in form of <uid>:<gid>")
	@NotEmpty
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
	@DependsOn(property = "provisionMode", value = "DOCKER")
	@NotNull
	public List<@NotNull @Valid RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}

	@Editable(order = 1550, group = "More Settings", description = "Optionally define config files for the workspace")
	@DependsOn(property = "provisionMode", value = "HOST", inverse = true)
	public List<@Valid ConfigFile> getConfigFiles() {
		return configFiles;
	}

	public void setConfigFiles(List<ConfigFile> configFiles) {
		this.configFiles = configFiles;
	}

	@Editable(order = 1600, group = "More Settings", name = "Caches", description = """
			Optionally define caches for the workspace. Cache populated by a workspace will be uploaded 
			after the workspace is deleted, and can be used by other workspaces created after it""")
	public List<@Valid CacheConfig> getCacheConfigs() {
		return cacheConfigs;
	}

	public void setCacheConfigs(List<CacheConfig> cacheConfigs) {
		this.cacheConfigs = cacheConfigs;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean isValid = true;

		Set<String> seenCachePaths = new HashSet<>();
		for (int i = 0; i < cacheConfigs.size(); i++) {
			var cacheConfig = cacheConfigs.get(i);
			for (int j = 0; j < cacheConfig.getEntries().size(); j++) {
				var path = cacheConfig.getEntries().get(j).getPath();
				if (path == null || path.isEmpty())
					continue;
				if (!path.contains("@")) {
					if (getProvisionMode() == ProvisionMode.HOST && FilenameUtils.getPrefixLength(path) > 0) {
						context.buildConstraintViolationWithTemplate(""
									+ "Item #" + (i + 1) + ": absolute path '"
									+ path + "' is not allowed for host provision mode")
								.addPropertyNode(PROP_CACHE_CONFIGS).addConstraintViolation();
						isValid = false;
					}
				}
				if (!seenCachePaths.add(path)) {
					context.buildConstraintViolationWithTemplate(
							"Item #" + (i + 1) + ": duplicate path '" + path + "'")
							.addPropertyNode(PROP_CACHE_CONFIGS).addConstraintViolation();
					isValid = false;
				}
			}
		}

		Set<String> seenUserDataPaths = new HashSet<>();
		for (int i = 0; i < userDatas.size(); i++) {
			var userData = userDatas.get(i);
			for (int j = 0; j < userData.getEntries().size(); j++) {
				String path = userData.getEntries().get(j).getPath();
				if (path != null && !path.isEmpty() && !seenUserDataPaths.add(path)) {
					context.buildConstraintViolationWithTemplate(
							"Item #" + (i + 1) + ": duplicate path '" + path + "'")
							.addPropertyNode(PROP_USER_DATAS).addConstraintViolation();
					isValid = false;
				}
			}
		}

		Set<String> seenConfigFilePaths = new HashSet<>();
		for (int i = 0; i < configFiles.size(); i++) {
			var configFile = configFiles.get(i);
			String path = configFile.getPath();
			if (path != null && !path.isEmpty() && !seenConfigFilePaths.add(path)) {
				context.buildConstraintViolationWithTemplate(
						"Item #" + (i + 1) + ": duplicate path '" + path + "'")
						.addPropertyNode(PROP_CONFIG_FILES).addConstraintViolation();
				isValid = false;
			}
		}

		if (!isValid)
			context.disableDefaultConstraintViolation();
		return isValid;
	}

	protected static List<InputSuggestion> suggestVariables(String matchWith) {
		return SuggestionUtils.suggestWorkspaceVariables(matchWith);
	}

	public void onRenameUser(String oldName, String newName) {
		if (taskAutomation != null)
			taskAutomation.onRenameUser(oldName, newName);
	}

	public Usage onDeleteUser(String userName) {
		if (taskAutomation != null) {
			if (taskAutomation.getApplicableAis().removeIf(userName::equals) 
					&& taskAutomation.getApplicableAis().isEmpty()) {
				taskAutomation = null;
			}
		}
		return new Usage();
	}

}
